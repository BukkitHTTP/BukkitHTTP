模块开发流程
--
BukkitHTTP的模块开发非常简单。  
我们将在五分钟内为您介绍如何开发一个BukkitHTTP模块。

#### 1 - 创建一个新的Java项目，并导入BukkitHTTP.jar为依赖项。

我们无法为这一步提供更详细的指导，因为这取决于您使用的IDE。

> 不过，以IDEA为例，您可以：  
> 创建一个新的项目。  
> 语言选择Java，构建工具选择Intellij。  
> 点击 文件->项目结构->(项目设置)库->+号(新建项目库)->Java->从磁盘选择BukkitHTTP.jar。  
> 点击 (项目设置)工件->+号(添加)->jar->空。  
> 双击右侧列表中的“[项目名称]编译输出”，使其进入左侧列表。  
> 勾选包含在项目构建中。  
> 最后点击确定即可。  
> 这样，您就可以在项目中使用BukkitHTTP了。  
> 项目的编译产物将在out\artifacts\[项目名称]目录下。

#### 2 - 创建一个主类。

主类可以在任何包下，拥有任何名称。  
主类应该继承BukkitServerProvider类。

#### 3 - 添加实现代码。

```
public void onEnable(String name,File dir,String uri){}
public void onDisable(){}
public Response serve(String uri,String method,Properties header,Properties parms,Properties files){}
public Response fallback(String uri,String method,Properties header,Properties parms,Properties files){}
```

以上的四个方法的作用如下：

- onEnable：当模块被加载时调用。您应该在这里初始化您的模块。
  尽可能避免在构造函数中进行初始化，或者使用static块。
  您可以保存name,dir,uri这三个参数，以便在后续的方法中使用。
    - name指的是模块名称。您可以基于此创建指令等。
    - dir指的是模块的文件夹路径。
        - 我们推荐您将您模块的全部资源放在本文件夹内。
        - 我们推荐您将配置文件命名为config.properties。
    - uri指的是用户为当前模块分配的路由。
- onDisable：当模块被卸载时调用。您应该在这里保存您的数据。
  请不要忘记关闭您的线程，还原您的Hook，反注册您的命令（如果有）。
  > BukkitHTTP最大的使命之一就是解决很多基于Java的HTTP框架不能实现热重载的问题。  
  > 请不要无视善后工作！这很重要！
- serve：当用户访问当前模块的路由时调用。您应该在这里处理用户的请求。
    - 如果返回null，BukkitHTTP将会认为您拒绝了路由，从而触发请求落回。
- fallback：当用户访问不存在的路由时调用。
    - 如果返回null，BukkitHTTP将会认为您拒绝处理这个落回问询，从而问询下一个插件。
    - 如果返回Response，BukkitHTTP将会中止落回问询，并且返回您给出的Response。

#### 4 - 打包，发布。

按照模块的定义完成即可。