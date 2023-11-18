使用HTTPS
--
自1.0.5 Pre2版本起，BukkitHTTP支持HTTPS与ACME。  
闲话休提。让我们开始教程吧。

### 0. 前置条件

首先，你需要安装JDK, OpenSSL, 和Certbot。  
参考下载地址：  
[JDK](https://adoptium.net/zh-CN/temurin/releases/?version=8)  
[OpenSSL](https://slproweb.com/products/Win32OpenSSL.html)  
[Certbot](https://github.com/certbot/certbot/releases/tag/v2.5.0)  
我们默认你已经配置了PATH。当然，你也可以修改以下的命令为安装目录来跳过PATH配置。

请将BukkitHTTP正常部署于您的服务器。  
例如，假如您的服务器位于 https.example.com ，那么请确保当您用浏览器访问https.example.com时，您能够看到BukkitHTTP的~~404~~
欢迎页面。

### 1. 获取证书

- 如果您拥有来自其他CA的证书，您可以跳过此步骤。

我们需要以手动模式运行Certbot。

````
certbot certonly --manual -d https.example.com
````

Certbot会先要求您输入邮箱地址和同意协议。  
一路确认即可，直到您看见这个提示：

````
(前略)
Create a file containing just this data:
TtHcKW-WTplCgT7MGfLxVIzLkDRrDamtWsN5wM5NAiY.lYizcLS8AvjjzncAI-xBuGmy3nrZUfflO2Vu45xI9BI
And make it available on your web server at this URL:
(后略)
````

此时，在您的BukkitHTTP终端中输入以下指令：

````
acme
````

再输入这行文本，按下回车。  
此时，您的BukkitHTTP终端应该会显示：

````
ACME is ready.
````

此时，您可以在Certbot的终端中按下回车。等待最多一分钟，您的证书就会生成。  
检查您的 C:\Certbot\archive\https.example.com 目录。

### 2. 格式转换

- 如果您拥有来自其他CA的其他格式的证书，请自行转换为p12格式再进行此步骤。
- 当然，你也可以直接转为jks格式。

您应该已经拿到了一组pem文件。  
取出其中叫做fullchain1.pem和privkey1.pem的文件。  
然后运行以下命令：

````
openssl pkcs12 -export -in fullchain1.pem -inkey privkey1.pem -out example.p12 -name mykey
````

您将会得到一个名为example.p12的文件。  
再运行以下命令：

````
keytool -importkeystore -srckeystore example.p12 -srcstoretype PKCS12 -destkeystore test.jks -deststoretype JKS -alias mykey
````

您将会得到一个名为test.jks的文件。

- 目前，BukkitHTTP只支持存储密码和密钥密码均为123456的且名称为test.jks的证书。
- 目前，BukkitHTTP只会在端口443上监听HTTPS请求。
- 本功能仍处于测试阶段，可能会出现未知的问题。

### 3. 配置BukkitHTTP

将test.jks文件放入您的BukkitHTTP目录。  
然后，打开您的BukkitHTTP配置文件，将端口改为443。  
完成后，重启BukkitHTTP。  
配置完成。

### 附1. 自签署证书或Cloudflare

在任意目录下运行以下命令以生成自签署证书：

````
keytool -genkeypair -alias mykey -keyalg RSA -keysize 2048 -validity 365 -keystore test.jks -storepass 123456 -keypass 123456 -dname "CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" && keytool -export -alias mykey -keystore test.jks -storepass 123456 -file test.cer && keytool -import -alias mykey -keystore test.jks -storepass 123456 -file test.cer
````

然后执行步骤3。  
如果是Cloudflare，请参考[这篇文章](https://developers.cloudflare.com/ssl/origin-configuration/ssl-modes).  
（太长不看？SSL/TLS开成完全[英文原文是Full]就行。比起默认，这样可以阻止一般的MITM。客户端看到的也是证书有效。）  
Cloudflare记得去配置文件关掉防火墙。  
