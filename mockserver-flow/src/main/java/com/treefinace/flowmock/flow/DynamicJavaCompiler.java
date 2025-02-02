package com.treefinace.flowmock.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicJavaCompiler {
    final Logger logger = LoggerFactory.getLogger(getClass());

    //存放编译之后的字节码(key:类全名,value:编译之后输出的字节码)
    private static Map<String, ByteJavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();
    //获取java的编译器
    private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    //存放编译过程中输出的信息
    private static DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();

    /**
     * 编译字符串源代码,编译失败在 diagnosticsCollector 中获取提示信息
     *
     * @return true:编译成功 false:编译失败
     */
    public boolean compiler(String sourceCode) {
        String fullClassName = getFullClassName(sourceCode);
        //标准的内容管理器,更换成自己的实现，覆盖部分方法
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
        JavaFileManager javaFileManager = new StringJavaFileManage(standardFileManager);
        //构造源代码对象
        JavaFileObject javaFileObject = new StringJavaFileObject(fullClassName, sourceCode);
        //获取一个编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticsCollector, null, null, Arrays.asList(javaFileObject));
        return task.call();
    }


    public void invoke(String sourceCode, String executor, Object[] args) {
        try {
            String fullClassName = getFullClassName(sourceCode);
            if (!javaFileObjectMap.containsKey(fullClassName)) {
                if (compiler(sourceCode)) {
                    logger.info("compiler sourceCode success : sourceCode={}", sourceCode);
                } else {
                    logger.error("compiler sourceCode failed : sourceCode={}, msg={}", sourceCode, getCompilerMessage());
                }
            }
            StringClassLoader scl = new StringClassLoader();
            Class<?> clazz = scl.findClass(fullClassName);
            Method executorMethod = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(executor)) {
                    executorMethod = method;
                }
            }
            Object targetObject = clazz.newInstance();
            executorMethod.invoke(targetObject, args);
        } catch (Exception e) {
            logger.error("dynamic running sourecode : {}", sourceCode, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 编译信息(错误 警告)
     */
    public String getCompilerMessage() {
        StringBuilder sb = new StringBuilder();
        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic.toString()).append("\r\n");
        }
        return sb.toString();
    }


    /**
     * 获取类的全名称
     *
     * @param sourceCode 源码
     * @return 类的全名称
     */
    public static String getFullClassName(String sourceCode) {
        String packagePath = sourceCode.substring(sourceCode.indexOf("package "));
        packagePath = packagePath.substring(0, packagePath.indexOf(";"))
            .replaceAll("package", "").replaceAll(";", "").trim();

        String className = sourceCode.substring(sourceCode.indexOf("class "));
        className = className.substring(0, className.indexOf("{")).replaceFirst("class", "").replace("{", "").trim();
        return packagePath + "." + className;
    }

    /**
     * 自定义一个字符串的源码对象
     */
    private class StringJavaFileObject extends SimpleJavaFileObject {
        //等待编译的源码字段
        private String contents;

        //java源代码 => StringJavaFileObject对象 的时候使用
        public StringJavaFileObject(String className, String contents) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        //字符串源码会调用该方法
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }

    }

    /**
     * 自定义一个编译之后的字节码对象
     */
    private class ByteJavaFileObject extends SimpleJavaFileObject {
        //存放编译后的字节码
        private ByteArrayOutputStream outPutStream;

        public ByteJavaFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), kind);
        }

        //StringJavaFileManage 编译之后的字节码输出会调用该方法（把字节码输出到outputStream）
        @Override
        public OutputStream openOutputStream() {
            outPutStream = new ByteArrayOutputStream();
            return outPutStream;
        }

        //在类加载器加载的时候需要用到
        public byte[] getCompiledBytes() {
            return outPutStream.toByteArray();
        }
    }

    /**
     * 自定义一个JavaFileManage来控制编译之后字节码的输出位置
     */
    private class StringJavaFileManage extends ForwardingJavaFileManager {
        StringJavaFileManage(JavaFileManager fileManager) {
            super(fileManager);
        }

        //获取输出的文件对象，它表示给定位置处指定类型的指定类。
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
            javaFileObjectMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    /**
     * 自定义类加载器, 用来加载动态的字节码
     */
    private class StringClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteJavaFileObject fileObject = javaFileObjectMap.get(name);
            if (fileObject != null) {
                byte[] bytes = fileObject.getCompiledBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            try {
                return ClassLoader.getSystemClassLoader().loadClass(name);
            } catch (Exception e) {
                return super.findClass(name);
            }
        }
    }
}