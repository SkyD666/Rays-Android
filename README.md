<div align="center">
    <div>
        <img src="image/Rays.svg" style="height: 210px"/>
    </div>
    <h1>🥰 Rays (Android)</h1>
    <p>
        <a href="https://github.com/SkyD666/Rays-Android/actions" style="text-decoration:none">
            <img src="https://img.shields.io/github/actions/workflow/status/SkyD666/Rays-Android/pre_release.yml?branch=master&style=for-the-badge" alt="GitHub Workflow Status"  />
        </a>
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
        <b>Rays (Record All Your Stickers)</b>, A tool to <b>record, search and manage stickers</b> on your phone.
    </p>
    <p>
        🥰 Are you still struggling with <b>too many stickers on your phone</b> and having trouble finding the ones you want? This tool will help you <b>manage your stickers</b>! 😋
    </p>
    <p>
        Rays utilizes the <b><a href="https://developer.android.com/topic/architecture#recommended-app-arch">MVI</a></b> architecture and fully adopts the <b><a href="https://m3.material.io/">Material You</a></b> design style. All pages are developed using <b> <a href="https://developer.android.com/jetpack/compose">Jetpack Compose</a></b>.
    </p>
    <p>
        <b><a href="https://hosted.weblate.org/engage/rays/">Help us translate</a></b>
    </p>
</div>

## 💡 Features

1. Support **tagging** for stickers
2. Support setting **search domains** (set the **fields of the database tables** to be searched)
3. Support searching using **regular expressions**
4. Support **recognizing text** in stickers
5. Support **recommending sticker tags** using **AI** (support change models)
6. Support **searching similar stickers**
7. Support **syncing data using WebDAV and files**
8. Support **AI image style transfer**
9. Support importing some stickers through the **system "Share" page**
10. Support **Monet theming and customizing theme colors**, support **theme color following the sticker's main color**
11. Support **dark color mode**
12. ......

## 🤩 Screenshots

![ic_home_screen](image/en/ic_home_screen.jpg) ![ic_home_screen_search](image/en/ic_home_screen_search.jpg)
![ic_add_screen_add](image/en/ic_add_screen_add.jpg) ![ic_share](image/en/ic_share.jpg)
![ic_appearance_screen](image/en/ic_appearance_screen.jpg) ![ic_import_export_screen](image/en/ic_import_export_screen.jpg)
![ic_mini_tool_screen](image/en/ic_mini_tool_screen.jpg) ![ic_style_transfer_screen](image/en/ic_style_transfer_screen.jpg)
![ic_search_config_screen](image/en/ic_search_config_screen.jpg) ![ic_more_screen](image/en/ic_more_screen.jpg)
![ic_settings_screen](image/en/ic_settings_screen.jpg) ![ic_classification_screen](image/en/ic_classification_screen.jpg)
![ic_auto_share_screen](image/en/ic_auto_share_screen.jpg) ![ic_about_screen](image/en/ic_about_screen.jpg)

## 🔍 Search examples

<table>
<thead>
  <tr>
    <th>Intent</th>
    <th>Text input in the search bar when using regular expression</th>
    <th>Text input in the search bar when regular expression is not used</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td>Search for content with the keyword "Genshin"</td>
    <td>.*Genshin.*</td>
    <td>Genshin</td>
  </tr>
  <tr>
    <td>Search only for the word "Genshin"</td>
    <td>Genshin or ^Genshin$</td>
    <td>⚠️Impossible</td>
  </tr>
  <tr>
    <td>Search for content with the keywords "crazy" or "Genshin"</td>
    <td>.*crazy.*|.*Genshin.*</td>
    <td>⚠️Impossible</td>
  </tr>
  <tr>
    <td>Search only for the word "crazy" or only for the word "Genshin"</td>
    <td>crazy|Genshin or ^crazy$|^Genshin$</td>
    <td>⚠️Impossible</td>
  </tr>
  <tr>
    <td>Search for content with the keywords "crazy" and "Genshin"</td>
    <td>.*crazy.*   .*Genshin.*</td>
    <td>crazy   Genshin</td>
  </tr>
  <tr>
    <td>Search for content with the keywords ("crazy" and with "Genshin") or "ikun"</td>
    <td>.*crazy.*|.*ikun.*   .*Genshin.*|.*ikun.*</td>
    <td>⚠️Impossible</td>
  </tr>
</tbody>
</table>
<p>Note: <b>and</b> logic is represented by <b>space, tab, line break</b>, multiple of the above characters together are considered as one, extra spaces before and after the input box text will be ignored. The <b>"content"</b> in the form refers to the selected search domain (the results of multiple search domains are combined).</p>

## 🌏 Translation

If you are interested, please help us **translate**, thank you.

<a href="https://hosted.weblate.org/engage/rays/">
<img src="https://hosted.weblate.org/widget/rays/string-xml/multi-auto.svg" alt="translate" />
</a>

## 🛠 Primary technology stack

- Jetpack **Compose**
- **MVI** Architecture
- Kotlin ﻿**Coroutines and Flow**
- **Material You**
- **ViewModel**
- **Hilt**
- **DataStore**
- **LiteRT**
- **ML Kit** (Machine Learning)
- **Room**
- **Paging**
- **AIDL**
- Splash Screen
- Navigation
- Coil
- Lottie
- Profile Installer

## 🤖 Machine learning

- Sticker classification: https://github.com/SkyD666/StickerClassification

## ✨ Other works

<table>
<thead>
  <tr>
    <th>Work</th>
    <th>Description</th>
    <th>Link</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td><img src="image/PodAura.svg" style="height: 100px"/></td>
    <td><b>PodAura</b>, an <b>all-in-one Podcast tool</b> for <b>RSS subscription and updates</b>, <b>media downloads</b> and <b>playback</b>.</td>
    <td><a href="https://github.com/SkyD666/PodAura">https://github.com/SkyD666/PodAura</a></td>
  </tr>
  <tr>
    <td><img src="image/Raca.svg" style="height: 100px"/></td>
    <td><b>Raca (Record All Classic Articles)</b>, a tool to <b>record and search abstract passages and mini-essays</b> in the comments section locally. 🤗 Are you still having trouble remembering the content of your mini-essay and facing the embarrassing situation of forgetting the front, middle and back? Using this tool will help you <b>record the mini-essays</b> you come across and never worry about forgetting them again! 😋</td>
    <td><a href="https://github.com/SkyD666/Raca-Android">https://github.com/SkyD666/Raca-Android</a></td>
  </tr>
  <tr>
    <td><img src="image/NightScreen.svg" style="height: 100px"/></td>
    <td><b>NightScreen</b>, when you <b>use your phone at night</b> 🌙, Night Screen can help you <b>reduce the brightness</b> of the screen and <b>reduce the damage to your eyes</b>.</td>
    <td><a href="https://github.com/SkyD666/NightScreen">https://github.com/SkyD666/NightScreen</a></td>
  </tr>
</tbody>
</table>


## 📃 License

This software code is available under the following **license**

[**GNU General Public License v3.0**](LICENSE)
