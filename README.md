# meta-loongarch

`meta-loongarch` 是用于loongarch架构的yocto bsp层。目前oe上游已部分支持loongarch架构，但仍然有些包不支持loongarch，我们对其进行了最小修改以支持loongarch（目前主要是qemuloongarch64机器），结合该层，我们可以构建一个可在qemu上运行的loongarch架构的操作系统以及对应的sdk。

## 构建

### 准备源码

运行以下命令，克隆相关软件仓库：

```
mkdir -p loong-yocto/downloads
cd loong-yocto
git clone https://github.com/openembedded/bitbake.git
git clone -b mickledore https://github.com/openembedded/openembedded-core.git
git clone -b mickledore https://git.yoctoproject.org/poky.git
git clone -b mickledore https://github.com/loongarch64/meta-loongarch.git
```


- `downloads` 目录用于保存下载的软件源代码，可在多次构建之间共享。

### 构建发行版

`build.sh` 默认会构建 `OpenEmbedded` 发行版，同时也支持构建 `Poky` 发行版。

可通过设置环境变量 `DISTRO` 来明确指定发行版，比如：

```
cd loong-yocto
./meta-loongarch/build.sh                          # Default, to build OpenEmbedded distro
DISTRO=openembedded-core ./meta-loongarch/build.sh # Build the OpenEmbedded Distro
DISTRO=poky ./meta-loongarch/build.sh              # Build the Poky Distro
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
TARGET=core-image-full-cmdline ./meta-loongarch/build.sh
```

## 调试

在编译发生错误时，可在 `build.sh` 脚本后面指定 build file，针对单一目标来构建，方便检查错误和调试。

当前支持以下几种用法：

```
./meta-loongarch/build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb
./meta-loongarch/build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb do_fetch
./meta-loongarch/build.sh openembedded-core/meta/recipes-kernel/linux/linux-yocto_6.1.bb:do_kernel_version_sanity_check
```

## 提示

qemu-system-longarch64需要bios。目前，edk2-loongarch提供了它的二进制代码。未来应更改为从上游拉取生成。Poky有ovmf配方，但目前还不支持loongarch。
