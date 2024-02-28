# meta-loongarch

`meta-loongarch` 是用于loongarch架构的yocto bsp层。目前oe上游已部分支持loongarch架构，但仍然有些包不支持loongarch，我们对其进行了最小修改以支持loongarch（目前主要是qemuloongarch64机器），结合该层，我们可以构建一个可在qemu上运行的loongarch架构的操作系统以及对应的sdk。

## 构建

### 1.准备源码

基于WSL2 Debian构建，安装相关包，**使用非root用户编译**
``` shell
sudo apt install gawk wget git diffstat unzip texinfo gcc build-essential chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils iputils-ping python3-git python3-jinja2 libegl1-mesa libsdl1.2-dev python3-subunit mesa-common-dev zstd liblz4-tool file locales tmux
```

a.克隆poky仓库，进入目录

``` shell
cd ~
git clone https://github.com/yoctoproject/poky.git
cd poky
```
b.基于yocto-4.3适配, 切换到对应tag
``` shell
git checkout -b loong yocto-4.3
```

c.克隆loongarch bsp层
``` shell
git clone https://github.com/otomam/meta-loongarch.git
```

### 2.构建发行版
a.配置构建环境，使用loongarch的配置文件
``` shell
export TEMPLATECONF=$PWD/meta-loongarch/conf/templates/default
. oe-init-build-env
```
a.构建镜像
``` shell
bitbake core-image-minimal
``````
可选：
- core-image-minimal
- core-image-full-cmdline
- core-image-sato
- core-image-weston
- meta-toolchain
- meta-ide-support

#### 构建报错

##### 例如构建llvm-native报错
```shell
| g++: fatal error: Killed signal terminated program cc1plus
| compilation terminated.
```
+ 可以单独编译并查看详细输出  
```
bitbake llvm-native -v -D
```
+ 这是在构建时内存不足导致的，可以单独构建或重复构建，或者根据实际情况减少并行编译的任务数或线程  
```
# ./build/local.conf
BB_NUMBER_THREADS ='8'
PARALLEL_MAKE = "-j 8"
```

### 3.虚拟机运行
+ **不带nographic参数可以同时看到串口输出和图形窗口**
``` shell
runqemu nographic serialstdio
```
+ **默认用户root，无密码**

##### 遇到报错
``` shell
runqemu - ERROR - Error: There are no available tap devices to use for networking,
runqemu - ERROR - and I see /etc/runqemu-nosudo exists, so I am not going to try creating
runqemu - ERROR - a new one with sudo.
```
##### 运行以下命令
``` shell
sudo ../scripts/runqemu-gen-tapdevs 1000 4
```
+ 作用：**Creating 4 tap devices for GID: 1000**