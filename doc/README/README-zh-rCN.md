<div align="center">
    <div>
        <img src="../../image/Rays.svg" style="height: 210px"/>
    </div>
    <h1>🥰 Rays (Android)</h1>
    <p>
        <a href="https://github.com/SkyD666/Rays-Android/releases/latest" style="text-decoration:none">
            <img src="https://img.shields.io/github/v/release/SkyD666/Rays-Android?display_name=release&style=for-the-badge" alt="GitHub release (latest by date)"/>
        </a>
        <a href="https://github.com/SkyD666/Rays-Android/releases/latest" style="text-decoration:none" >
            <img src="https://img.shields.io/github/downloads/SkyD666/Rays-Android/total?style=for-the-badge" alt="GitHub all downloads"/>
        </a>
        <a href="https://www.android.com/versions/nougat-7-0" style="text-decoration:none" >
            <img src="https://img.shields.io/badge/Android 7.0+-brightgreen?style=for-the-badge&logo=android&logoColor=white" alt="Support platform"/>
        </a>
        <a href="https://github.com/SkyD666/Rays-Android/blob/master/LICENSE" style="text-decoration:none" >
            <img src="https://img.shields.io/github/license/SkyD666/Rays-Android?style=for-the-badge" alt="GitHub license"/>
        </a>
        <a href="https://t.me/SkyD666Chat" style="text-decoration:none" >
            <img src="https://img.shields.io/badge/Telegram-2CA5E0?logo=telegram&logoColor=white&style=for-the-badge" alt="Telegram"/>
        </a>
        <a href="https://discord.gg/pEWEjeJTa3" style="text-decoration:none" >
            <img src="https://img.shields.io/discord/982522006819991622?color=5865F2&label=Discord&logo=discord&logoColor=white&style=for-the-badge" alt="Discord"/>
        </a>
    </p>
    <p>
        <b>Rays (Record All Your Stickers)</b>，一个在本地<b>记录、查找、管理表情包</b>的工具。
    </p>
    <p>
        🥰 您还在为手机中的<b>表情包太多</b>，找不到想要的表情包而苦恼吗？使用这款工具将帮助您<b>管理您存储的表情包</b>，再也不因为找不到表情包而烦恼！😋
    </p>
    <p>
        使用<b> <a href="https://developer.android.com/topic/architecture#recommended-app-arch">MVI</a> </b>架构，完全采用<b> <a href="https://m3.material.io/">Material You</a> </b>设计风格。<b>所有页面均使用 <a href="https://developer.android.com/jetpack/compose">Jetpack Compose</a> </b>开发。
    </p>
    <p>
        <b><a href="../../README.md">English</a></b>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
        <b><a href="https://github.com/SkyD666/Rays-Android/issues/4">帮助我们翻译</a></b>
    </p>
</div>




## 💡主要功能

1. 支持为表情包打**标签**
2. 支持设置**搜索域**（设置搜索**数据库表的字段**）
3. 支持使用**正则表达式搜索**
4. 支持**识别**表情包中的**文本**
5. 支持 **Ai 推荐表情包标签**（支持更换模型）
6. 支持**使用 WebDAV 同步**数据
7. 支持通过**系统“分享”页面导入**表情包
8. 支持**更换和自定义主题色**，支持主题色**跟随表情包主色调**变化
9. 支持**深色模式**
10. ......

## 🤩应用截图

![ic_home_screen](../../image/zh-rCN/ic_home_screen.jpg) ![ic_home_screen_search](../../image/zh-rCN/ic_home_screen_search.jpg)
![ic_add_screen_edit](../../image/zh-rCN/ic_add_screen_edit.jpg) ![ic_home_screen_share](../../image/zh-rCN/ic_home_screen_share.jpg)
![ic_appearance_screen](../../image/zh-rCN/ic_appearance_screen.jpg) ![ic_webdav_screen](../../image/zh-rCN/ic_webdav_screen.jpg)
![ic_search_config_screen](../../image/zh-rCN/ic_search_config_screen.jpg) ![ic_more_screen](../../image/zh-rCN/ic_more_screen.jpg)
![ic_settings_screen](../../image/zh-rCN/ic_settings_screen.jpg) ![ic_classification_screen](../../image/zh-rCN/ic_classification_screen.jpg)
![ic_auto_share_screen](../../image/zh-rCN/ic_auto_share_screen.jpg) ![ic_about_screen](../../image/zh-rCN/ic_about_screen.jpg)

## 🔍搜索示例

<table>
<thead>
  <tr>
    <th>意图</th>
    <th>使用正则表达式时搜索栏输入的文字</th>
    <th>不使用正则表达式时搜索栏输入的文字</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td>搜索带有“原神”关键词的内容</td>
    <td>.*原神.*</td>
    <td>原神</td>
  </tr>
  <tr>
    <td>搜索仅为“原神”两个字的内容</td>
    <td>原神 或者 ^原神$</td>
    <td>⚠️无法实现</td>
  </tr>
  <tr>
    <td>搜索带有“发电” 或 带有“原神”关键词的内容</td>
    <td>.*发电.*|.*原神.*</td>
    <td>⚠️无法实现</td>
  </tr>
  <tr>
    <td>搜索仅为“发电”两个字 或 仅为“原神”两个字的内容</td>
    <td>发电|原神 或者 ^发电$|^原神$</td>
    <td>⚠️无法实现</td>
  </tr>
  <tr>
    <td>搜索带有“发电” 且 带有“原神”关键词的内容</td>
    <td>.*发电.*   .*原神.*</td>
    <td>发电   原神</td>
  </tr>
  <tr>
    <td>搜索带有（“发电” 且 带有“原神”） 或 带有“ikun”关键词的内容</td>
    <td>.*发电.*|.*ikun.*   .*原神.*|.*ikun.*</td>
    <td>⚠️无法实现</td>
  </tr>
</tbody>
</table>
注：**且** 逻辑使用 **空格、制表符、换行符** 表示，多个上述字符连接在一起时视为一个，输入框文字前后多余空格将被忽略。表格中的 **“内容”** 指的是选择的搜索域（多个搜索域的结果取并集）。

## 🛠主要技术栈

- Jetpack **Compose**
- **MVI** Architecture
- **Material You**
- **ViewModel**
- **Hilt**
- **TensorFlow** Lite
- **ML Kit** (Machine Learning)
- **DataStore**
- Room
- Splash Screen
- Navigation
- Lottie
- Profile Installer

## 🤖机器学习

- 表情包分类：https://github.com/SkyD666/StickerClassification

## ✨其他应用

<table>
<thead>
  <tr>
    <th>工具</th>
    <th>描述</th>
    <th>传送门</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td><img src="../../image/Raca.svg" style="height: 100px"/></td>
    <td><b>Raca (Record All Classic Articles)</b>，一个在本地<b>记录、查找抽象段落/评论区小作文</b>的工具。<br/>🤗 您还在为记不住小作文内容，面临<b>前面、中间、后面都忘了</b>的尴尬处境吗？使用这款工具将<b>帮助您记录您所遇到的小作文</b>，再也不因为忘记而烦恼！😋</td>
    <td><a href="https://github.com/SkyD666/Raca-Android">https://github.com/SkyD666/Raca-Android</a></td>
  </tr>
  <tr>
    <td><img src="../../image/NightScreen.svg" style="height: 100px"/></td>
    <td><b>NightScreen</b>，当您在<b>夜间🌙</b>使用手机时，NightScreen 可以帮助您<b>减少屏幕亮度</b>，减少对眼睛的伤害。</td>
    <td><a href="https://github.com/SkyD666/NightScreen">https://github.com/SkyD666/NightScreen</a></td>
  </tr>
</tbody>
</table>

## 📃许可证

使用此软件代码需**遵循以下许可证协议**

[**GNU General Public License v3.0**](../../LICENSE)
