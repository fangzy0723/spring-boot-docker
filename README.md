# spring-boot-docker
### spring boot应用发布到docker容器示例



#### 1、创建一个spring boot应用

使用idea编辑器或者使用<http://start.spring.io/> 网站创建一个spring boot 项目，pom.xml文件中添加docker插件配置，完整pom.xml文件内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>docker</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>docker</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <docker.image.prefix>springboot</docker.image.prefix>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- 指定JDK编译版本 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                </configuration>
            </plugin>

            <!-- Docker maven plugin -->
            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>1.2.0</version>
                <configuration>
                    <!--docker镜像名-->
                    <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                    <!--Dockerfile的路径-->
                    <dockerDirectory>src/main/docker</dockerDirectory>
                    <!--构建时所需要的资源文件，和Dockerfile放在一起-->
                    <resources>
                        <resource>
                            <targetPath>/</targetPath>
                            <directory>${project.build.directory}</directory>
                            <include>${project.build.finalName}.jar</include>
                        </resource>
                    </resources>
                </configuration>
            </plugin>

        </plugins>
    </build>

    <!-- Aliyun images repository -->
    <repositories>
        <repository>
            <id>central</id>
            <name>aliyun</name>
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
        </repository>
    </repositories>

</project>

```

#### 2、新建一个DockerController类，用来测试接口的调用

```java
/**
 * Created by fangzy on 2019/1/31 12:07
 */
@RestController
public class DockerController {

    @GetMapping("/docker")
    public String dockerTest(){
        return "docker test demo application1";
    }
}
```

#### 3、创建Dockerfile文件

在src/main下创建目录docker目录，在docker目录下创建Dockerfile文件，文件内容如下

```dockerfile
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD docker-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

这个 Dockerfile 文件很简单，构建 Jdk 基础环境，添加 Spring Boot Jar 到镜像中，简单解释一下:

- FROM ，表示使用 Jdk8 环境 为基础镜像，如果镜像不是本地的会从 DockerHub 进行下载
- VOLUME ，VOLUME 指向了一个`/tmp`的目录，由于 Spring Boot 使用内置的Tomcat容器，Tomcat 默认使用`/tmp`作为工作目录。这个命令的效果是：在宿主机的`/var/lib/docker`目录下创建一个临时文件并把它链接到容器中的`/tmp`目录
- ADD ，拷贝文件并且重命名
- ENTRYPOINT ，为了缩短 Tomcat 的启动时间，添加`java.security.egd`的系统属性指向`/dev/urandom`作为 ENTRYPOINT

#### 4、把项目上传到linux一个目录下，测试打包启动

```
//打包
mvn clean package
//启动
java -jar target/docker-0.0.1-SNAPSHOT.jar
```

> 启动成功之后浏览器中访问  `http://ip:port/docker`,出现docker test demo application1 说明测试打包启动正常



#### 5、使用docker部署项目

> 把target下的docker-0.0.1-SNAPSHOT.jar文件复制到src/main/docker下跟Dockerfile同级目录下

在pom文件同级目录下执行命令下面的命令，使用Dockerfile构建镜像文件

```
mvn package docker:build
```

日志输出下面内容说明镜像构建成功

```java
[INFO] Built springboot/docker
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 02:12 min
[INFO] Finished at: 2019-01-31T14:18:03+08:00
[INFO] ------------------------------------------------------------------------
```

查看镜像、启动容器

```
//使用命令查看构建的镜像文件
docker images
//启动容器
docker run -p 8880:8880 -d --name spring-boot-docker springboot/docker
	//-p:端口映射
	//-d:后台启动
	//--name：指定容器名称
	//springboot/docker：镜像名
//查看启动的容器
docker ps
```

执行命令下面的命令测试容器的启动

```
curl localhost:8880/docker
```

返回`docker test demo application1`，则说明服务在容器中正常启动



#### 参考

[使用 Docker 部署 Spring Boot](http://www.imooc.com/article/25621)

[Spring Boot应用发布到Docker](https://lw900925.github.io/docker/docker-springboot.html)

#### 示例完整代码

https://github.com/fangzy0723/spring-boot-docker/tree/master/docker

