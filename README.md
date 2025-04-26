# meta-loongarch

`meta-loongarch` 是用于loongarch架构的yocto bsp层。目前oe上游已部分支持loongarch架构，但仍然有些包不支持loongarch，我们对其进行了最小修改以支持loongarch（目前主要是qemuloongarch64机器），结合该层，我们可以构建一个可在qemu上运行的loongarch架构的操作系统以及对应的sdk。

## 构建

### 准备源码

运行以下命令，克隆相关软件仓库：

```
mkdir -p loong-yocto/downloads
cd loong-yocto
repo init -u https://github.com/loongarch64/meta-loongarch  -b master -m tools/manifests/loong-yocto.xml
repo sync
repo start work --all
```

- `downloads` 目录用于保存下载的软件源代码，可在多次构建之间共享。

### 更新已存在的本地代码

运行以下代码，以保持和上游同步

```
cd loong-yocto
repo sync
repo rebase
```

### 构建发行版

运行 `build.sh` 命令默认构建:

```
cd loong-yocto
./build.sh
```

### 构建目标

`build.sh` 脚本默认构建目标为 `core-image-minimal`, 可支持以下公共目标：

- core-image-minimal
- core-image-full-cmdline
- core-image-sato
- core-image-weston
- meta-toolchain
- meta-ide-support

要指定编译不同的目标，可通过设置环境变量 `TARGET` 来完成，比如：

```
cd loong-yocto
TARGET=core-image-full-cmdline ./build.sh
```

## 调试

在编译发生错误时，可在 `build.sh` 脚本后面指定 build file，针对单一目标来构建，方便检查错误和调试。

当前支持以下几种用法：

```
./build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb
./build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb do_fetch
./build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb:do_kernel_version_sanity_check
```
