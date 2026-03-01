# NoCheatPlus

Language: [English](./README.md) | **简体中文**

[![Build Status](https://ci.codemc.io/buildStatus/icon?job=Updated-NoCheatPlus%2FUpdated-NoCheatPlus)](https://ci.codemc.io/job/Updated-NoCheatPlus/job/Updated-NoCheatPlus/)
[![Discord](https://img.shields.io/discord/598285007496151098?label=discord&logo=discord)](https://discord.gg/NASKHYc)

Updated-NoCheatPlus 是经典反作弊插件 NoCheatPlus 的延续分支。

## GPLv3 署名与合规说明（Fork Notice）

本仓库是基于 NoCheatPlus 的社区 fork，可能包含维护者的二次修改。

为最大程度尊重原作者并符合 GPLv3：
- 派生代码继续使用 **GPLv3**。
- 保留并明确标注上游作者与维护者署名。
- 修改代码保持开源可获取，不进行闭源衍生分发。
- 不宣称上游项目归属；本仓库仅为 fork 与维护分支。

上游作者与项目致谢：
- NoCheat 原始作者：[Evenprime](https://github.com/Evenprime)
- NoCheatPlus 作者：[NeatMonster](https://github.com/NeatMonster)、[Asofold](https://github.com/asofold)
- 延续维护：[Updated-NoCheatPlus](https://github.com/Updated-NoCheatPlus/NoCheatPlus)

分发本 fork 构建产物时，请同时提供：
- `LICENSE.txt`（GPLv3）
- 本 fork 源码链接
- 明确的“fork/已修改”说明

## 安装（简版）

1. 从本 fork 的 **[Releases](https://github.com/sddlol/NoCheatPlus/releases)** 下载 `NoCheatPlus.jar`
2. 放到服务器 `plugins/` 目录
3. 重启服务器（不建议 `/reload`）

## 快速配置（本 fork 的证据 profile）

本 fork 增加了统一的证据融合配置：`checks.combined.evidence.*`（用于 staged Improbable 升级）。

**示例：全局 strict + 局部 balanced 覆盖**

```yaml
checks:
  combined:
    evidence:
      profile: strict
      overrides:
        moving-timer: balanced
        net-keepalivefrequency: balanced
      debug:
        active: true
        min-interval-ms: 1500
```

可覆盖键：
- `moving-timer`、`moving-velocity`
- `fight-reach`
- `blockplace-reach`、`blockplace-scaffold`
- `net-attackfrequency`、`net-flyingfrequency`、`net-wrongturn`、`net-keepalivefrequency`、`net-packetfrequency`

取值：
- 全局 `profile`：`balanced` / `strict`
- `overrides.*`：`inherit` / `balanced` / `strict`

详细说明：Docs → [Combined Improbable](https://github.com/sddlol/Docs/blob/master/zh-CN/Settings/Checks/%5BCombined%5D-Improbable.md)

## 构建

```bash
mvn clean package
```

如需包含非公开兼容模块，可参考英文 README 的 Profiles 说明。

## 相关链接

- 文档与配置：<https://github.com/Updated-NoCheatPlus/Docs>
- 权限说明：<https://github.com/Updated-NoCheatPlus/Docs/blob/master/Settings/Permissions.md>
- 命令说明：<https://github.com/Updated-NoCheatPlus/Docs/blob/master/Settings/Commands.md>
- 许可证（GPLv3）：<https://github.com/Updated-NoCheatPlus/NoCheatPlus/blob/master/LICENSE.txt>

> 完整英文说明请查看 [README.md](./README.md)。
