# JarDemo
Android系统二次开发包导入技巧

# 前言
之前在平台发布过一篇关于AS导入二次开发系统包的文章，得到很多开发者的回馈和讨论，有兴趣的可以回看[AS中导入framework.jar包编译，运行全部通过]([https://www.jianshu.com/p/36cd2a7c888d](https://www.jianshu.com/p/36cd2a7c888d)
)。加上最近使用团队开发模式，楼主也用到了Mac环境做开发。一些android/framework包的路径问题也暴露出来。小伙伴们都抱怨每次编译前都需要去project目录下build.gradle修改相关的路径参数。
![小妖怪](https://upload-images.jianshu.io/upload_images/2326194-7956df7fbed4b38f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/811/format/webp)
抱着方便你(zhuang)我(bi)他，就研究了一些获取系统路径参数的办法。

# 准备
首先，准备一个系统包，![系统包](https://upload-images.jianshu.io/upload_images/2326194-83e949dc2365a7c1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)。如果手上没有，可以在这里下载------[android.jar包](https://raw.githubusercontent.com/jixiang52002/document-library/master/android/android.jar)。这个包更改了网络部分，可以在自己项目中试试以下语句，会报错
```
LinkAddress mIpAddress = new LinkAddress(mIpAddr, prefixLength);
```
错误信息如下：
```
错误: 无法将类 LinkAddress中的构造器 LinkAddress应用到给定类型;
需要: 没有参数
找到: InetAddress,int
原因: 实际参数列表和形式参数列表长度不同
```
打开源码，这段构造函数是hide的。
```

    /**
     * Constructs a new {@code LinkAddress} from an {@code InetAddress} and prefix length, with
     * the specified flags and scope. Flags and scope are not checked for validity.
     * @param address The IP address.
     * @param prefixLength The prefix length.
     * @param flags A bitmask of {@code IFA_F_*} values representing properties of the address.
     * @param scope An integer defining the scope in which the address is unique (e.g.,
     *              {@link OsConstants#RT_SCOPE_LINK} or {@link OsConstants#RT_SCOPE_SITE}).
     * @hide
     */
    public LinkAddress(InetAddress address, int prefixLength, int flags, int scope) {
        init(address, prefixLength, flags, scope);
    }
```
# 操作流程
首先，将android包放在lib目录下，并在项目的build.gradle目录的dependence下添加以下代码：

* gradle 3.0以上
   ```
   compileOnly files('libs/android.jar')
   ```
* gradle 3.0以下
```
provide files('libs/android.jar')
```
这里是为了在编译和打包时只编译/打包进使用过得二次编码包。因为使用compile或者implementation会将整个android.jar包打入apk包中，还会造成dex打包错误。同时注释以下依赖
```
//    implementation fileTree(include: ['*.jar'], dir: 'libs')
```

其次，这里无需再增加.iml的操作脚本。也就是如下脚本
```
//@Deprecated
preBuild {
    doLast {
        def imlFile = file(project.name + ".iml")
        println 'Change ' + project.name + '.iml order'
        try {
            def parsedXml = (new XmlParser()).parse(imlFile)
            def jdkNode = parsedXml.component[1].orderEntry.find { it.'@type' == 'jdk' }
            parsedXml.component[1].remove(jdkNode)
            def sdkString = "Android API " + android.compileSdkVersion.substring("android-".length()) + " Platform"
            new Node(parsedXml.component[1], 'orderEntry', ['type': 'jdk', 'jdkName': sdkString, 'jdkType': 'Android SDK'])
            groovy.xml.XmlUtil.serialize(parsedXml, new FileOutputStream(imlFile))
        } catch (FileNotFoundException e) {
            // nop, iml not found
        }
    }
}
```

最后，在项目根目录build.gradle目录下，需要使用全局配置路径的方式来引入jar包。
```
  //添加以下代码，使android.jar包编译先于系统下android.jar
        gradle.projectsEvaluated {
            tasks.withType(JavaCompile) {
                options.compilerArgs.add("-Xbootclasspath/p:$rootDir${File.separator}app${File.separator}libs${File.separator}android.jar")
            }
        }
```
* rootDir 项目的路径
* $ {File.separator}引用当前系统的分隔符，windows与mac不相同

编译运行通过。




