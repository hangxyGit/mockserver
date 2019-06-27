package com.treefinace.flowmock.flow;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.service.ProjectService;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mappers.HttpServletRequestToMockServerRequestDecoder;
import org.mockserver.mock.HttpStateHandler;
import org.mockserver.mock.action.ActionHandler;
import org.mockserver.responsewriter.ResponseWriter;
import org.mockserver.scheduler.Scheduler;
import org.mockserver.serialization.PortBindingSerializer;
import org.mockserver.socket.tls.KeyAndCertificateFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockserver.model.HttpResponse.response;

@Component
public class FlowMockServlet extends DispatcherServlet implements ServletContextListener, InitializingBean {

    private MockServerLogger mockServerLogger;
    // generic handling
    private FlowHttpStateHandler httpStateHandler;
    // serializers
    private PortBindingSerializer portBindingSerializer;
    // mappers
    private HttpServletRequestToMockServerRequestDecoder requestDecoder = new HttpServletRequestToMockServerRequestDecoder();
    // mockserver
    private ActionHandler actionHandler;

    private EventLoopGroup workerGroup = new NioEventLoopGroup(ConfigurationProperties.nioEventLoopThreadCount());

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ScriptProcessorManager scriptProcessorManager;
    @Value("${server.port}")
    private int serverPort;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        this.scheduler.shutdown();
        this.workerGroup.shutdownGracefully(0, 0, MILLISECONDS).syncUninterruptibly();
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        ResponseWriter responseWriter = new FlowServletResponseWriter(scriptProcessorManager, httpServletResponse);
        FlowHttpRequest request = null;
        try {

            // 接受mock 配置的后台端口，转至Controller处理
            if (httpServletRequest.getServerPort() == serverPort) {
                if (httpServletRequest.getPathInfo().startsWith(HttpStateHandler.PATH_PREFIX)) {
                    request = new FlowHttpRequest(requestDecoder.mapHttpServletRequestToMockServerRequest(httpServletRequest));
                    httpStateHandler.handle(request, responseWriter, false);
                } else {
                    super.service(httpServletRequest, httpServletResponse);
                }
                return;
            }

            request = new FlowHttpRequest(requestDecoder.mapHttpServletRequestToMockServerRequest(httpServletRequest));
            // 项目信息
            String project = request.getPrimaryPath().replaceAll("/", "");
            // 获取redis 配置
            request.setProject(projectService.getProject(project));

            final String hostHeader = request.getFirstHeader(HOST.toString());
            scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    KeyAndCertificateFactory.addSubjectAlternativeName(hostHeader);
                }
            });

            String portExtension = "";
            if (!(httpServletRequest.getLocalPort() == 443 && httpServletRequest.isSecure() || httpServletRequest.getLocalPort() == 80)) {
                portExtension = ":" + httpServletRequest.getLocalPort();
            }
            actionHandler.processAction(request, responseWriter, null, ImmutableSet.of(
                httpServletRequest.getLocalAddr() + portExtension,
                "localhost" + portExtension,
                "127.0.0.1" + portExtension
            ), false, true);
        } catch (IllegalArgumentException iae) {
            mockServerLogger.error(request, "exception processing: {} error: {}", request, iae.getMessage());
            // send request without API CORS headers
            responseWriter.writeResponse(request, BAD_REQUEST, iae.getMessage(), MediaType.create("text", "plain").toString());
        } catch (Exception e) {
            mockServerLogger.error(request, e, "exception processing " + request);
            responseWriter.writeResponse(request, response().withStatusCode(BAD_REQUEST.code()).withBody(e.getMessage()), true);
        }
    }

    /**
     * 刷新
     *
     * @throws Exception
     */
    @Scheduled(cron = "0/30 * * * * ? ")
    @Override
    public void afterPropertiesSet() throws Exception {
        RedisExpectationInitializer.setProjectService(projectService);
        this.httpStateHandler = new FlowHttpStateHandler(scheduler);
        this.mockServerLogger = httpStateHandler.getMockServerLogger();
        this.portBindingSerializer = new PortBindingSerializer(mockServerLogger);
        this.actionHandler = new FlowActionHandler(scriptProcessorManager, workerGroup, httpStateHandler, null);
    }

}
