<p align="center"><img src="docs/media/small-logo.svg" alt="Simple Deployment" width="100" height="100"></p>

<h1 align="center">Simple Deployment</h1>

<p align="center">Intellij platform plugin to facilitate the deployment of upgrade services during development</p>

<div align="center">
    <a href="#"><img src="https://badge.fury.io/gh/lin2j%2Fsimple-deployment.svg"></a>
    <a href="#"><img src = "https://img.shields.io/github/license/lin2j/simple-deploy" ></a>
    <a href="https://www.lin2j.tech"><img src="https://img.shields.io/badge/author-lin2j-brightgreen"></a>
    <a href="#"><img src="https://img.shields.io/badge/idea-193.5662%2B-yellow"></a>
</div>

[**简体中文**](README_zh_CN.md) 🐼

Simple Deployment is a plug-in developed by me using the Alibaba Cloud Toolkit plug-in to facilitate the deployment of services during the development process.

Compared with the Alibaba Cloud Toolkit, the plug-in functions I develop will be much less, because I can't use many functions of ACT, and it is a bit bloated for me.

Therefore, I only take the part of the functions that I care about for development, which is the process of server management and deployment.

I gave this plugin this name because its functionality is really seldom simple, but I will continue to optimize it and improve its functionality.

This is also the first IntelliJ platform plug-in I developed. Many problems were solved little by little after a long search. I also plan to write the process of developing plug-ins in the future.

Now that the plug-in has been released to the plug-in market, search and install it by the name Simple Deployment, or download the installation package for manual installation.


👉👉 [Find in plugin marketplace](https://plugins.jetbrains.com/plugin/19432-simple-deployment)

🪐🪐 [My Blog](https://www.lin2j.tech)

# Support

Simple Deployment need your support, please give a star on this repo if it helped you. ⭐️⭐ 

# Features

## Server management

You can use this plugin to add multiple servers, and then use this as a base for command execution, file uploading, and application deployment.

The login password of the server can be stored or not. If it is not stored, a password input box will pop up when needed.

After adding the server, you can establish an SSH connection directly from the Terminal button.

This function is to implement a subclass of AbstractTerminalRunner, so that the community version of Idea can also use this function.

In fact, there is a remote-run plugin under the `com.intellij.modules.ultimate` module that already has an implemented AbstractTerminalRunner, but the community version cannot use this plugin.

After thinking about it, I decided to implement AbstractTerminalRunner myself to take care of users of the community edition.

<img src="docs/media/Add-Server.gif" alt="add server">

## Command management

You can add some alternate commands to a server and execute them directly. Commands for each server are isolated, which facilitates command management.

When adding a command, you need to use an absolute path to specify the directory where the command is executed on the remote server.

At present, only simple commands can be executed. It is recommended to write scripts that are too complex and put them on the server before executing the scripts. I usually deploy services in this way.

If you use a command like `tail -f` that does not return all the information at once, it will cause the current thread to block, because the read stream has never been given a terminator.

<img src="docs/media/Command.gif" alt="command">

## File upload

You can upload local file using the upload button on the panel. At present, files cannot be downloaded from the server, and this function will not be added in the future.

I combine the local file and remote directory selected during upload and manage it as an upload configuration, so that the next time I use it, I don't need to repeat the selection of the local file and remote directory.

## Deployment

Only some simple deployment methods can be used here. First, upload the program files to the specified remote directory, and then execute the startup command in the directory. Because my daily deployment services are probably in this order.

The deployment plan is actually to select the command to be executed after uploading on the basis of uploading the configuration.

<img src="docs/media/Upload.gif" alt="upload">

# TODO

- [x] add, edit and remove servers
- [x] add, edit and delete command
- [x] upload file
- [x] service deployment
- [x] open terminal
- [ ] import and export configuration
- [ ] execute complex shell scripts
- [ ] server search
- [ ] multiple servers can be selected when executing command deployment

# contact me 🐾

I have no experience with plugin development yet so there are a lot of issues that may not be handled very well.

If you have any suggestions or encounter any bugs, you can raise issues or contact me by email, and I will reply you as soon as possible.

📮📮 linjinjia047@163.com

# License

[**MIT**](LICENSE).
