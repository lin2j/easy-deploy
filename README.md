<p align="center"><img src="docs/media/small-logo.svg" alt="Simple Deployment" width="100" height="100"></p>

<h1 align="center">Simple Deployment</h1>

---

<div align="center">
    <a href="#"><img src="https://img.shields.io/badge/version-1.2.2-red"></a>
    <a href="#"><img src = "https://img.shields.io/github/license/lin2j/simple-deploy" ></a>
    <a href="www.lin2j.tech"><img src="https://img.shields.io/badge/author-lin2j-brightgreen"></a>
    <a href="#"><img src="https://img.shields.io/badge/idea-213.5744%2B-yellow"></a>
</div>


# 前言

Simple Deployment 是我借鉴 Alibaba Cloud Toolkit 插件开发的一个方便自己在开发过程中部署服务的插件。

相比较 Aliabab Cloud Toolkit，我开发的插件功能会少很多很多，因为 ACT 很多的功能我都用不上，它对于我来说有点臃肿。

因此，我只取自己关注的那部分功能进行开发，这部分就是对服务器的管理以及发布的过程。

我给这个插件起这个名是因为它的功能真的很少很简单，不过我会不断地优化它，完善它的功能。

这也是我开发的第一款 Idea 插件，很多问题都是通过长时间的搜索才一点一点的解决的，后续我也打算将开发插件的过程写出来。

# 功能

## 服务器管理

你可以用这个插件添加多台服务器，然后以此为基础进行命令执行、文件上传和应用部署。

添加服务器之后，可以直接通过终端按钮建立一个SSH连接。

这个功能是自己实现了一个 AbstractTerminalRunner 的子类，这样社区版的 Idea 也可以使用这个功能。

其实旗舰版的平台下有一个 remote-run 插件已经有一个实现好的 AbstractTerminalRunner，只不过社区版用不了这个插件。

考虑之后，我还是决定自己实现一个 TerminalRunner，照顾一下社区版的用户。

![](/docs/media/Add-Server.gif)

## 命令管理

你可以为某一台服务器添加一些备用命令，然后直接执行。每一台服务器的命令都是隔离的，这样有助于命令的管理。

命令在添加的时候，需要使用绝对路径来指定命令执行时所在的目录。

目前只能执行简单的命令，太过复杂的命令建议写成脚本放到服务器之后再执行脚本，我平时部署服务也是用这种方式。

如果使用了像`tail -f`不是一次性返回所有信息的命令，会导致当前线程阻塞，因为读取的流一直没有给一个终止符。

![](/docs/media/Command.gif)

## 文件上传

你可以使用面板上的上传按钮进行本地文件的上传。目前并不能从服务器下载文件，以后大概率也不会加上这个功能。

我将上传时选择的本地文件和远程目录组合起来，作为一个上传配置来管理，这样可以下次使用时无需重复地进行本地文件和远程目录的选择。

## 部署

这里只能采用一些简单的部署方式，首先是上传程序文件到指定的远程目录，然后在该目录下执行启动命令。

部署方案其实就是在上传配置的基础上，选择上传后需要执行的命令。

![](/docs/media/Upload.gif)

# 待实现

- [x] 添加/删除服务器
- [x] 添加/编辑/删除命令
- [x] 上传文件
- [x] 部署服务
- [x] 打开终端
- [ ] 配置导入/导出
- [ ] 执行多行脚本
- [ ] 服务器搜索
- [ ] 执行命令/部署时可以选择多台服务器

# 联系我🐾

在开发 Idea 插件方面我是一个新手，有很多问题可能处理得不是很好。比如目前我不知道如何发布各个idea版本都适用的插件版本。

如果你有什么建议或者遇到什么bug，可以提 issues 也可以邮箱联系我，我会尽快回复你。

📮 linjinjia047@163.com

👉 https://github.com/lin2j/simple-deploy
