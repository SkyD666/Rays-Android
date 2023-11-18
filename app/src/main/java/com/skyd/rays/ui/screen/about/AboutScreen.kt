package com.skyd.rays.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skyd.rays.R
import com.skyd.rays.config.GITHUB_REPO
import com.skyd.rays.config.NIGHT_SCREEN_URL
import com.skyd.rays.config.RACA_ANDROID_URL
import com.skyd.rays.config.WEBLATE_URL
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.model.bean.OtherWorksBean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.component.shape.SquircleShape
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.about.license.LICENSE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.about.update.UpdateDialog
import com.skyd.rays.util.CommonUtil
import com.skyd.rays.util.CommonUtil.openBrowser

const val ABOUT_SCREEN_ROUTE = "aboutScreen"

@Composable
fun AboutScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    var openUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var openSponsorDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.about)) },
                actions = {
                    RaysIconButton(
                        imageVector = Icons.Default.Balance,
                        contentDescription = stringResource(id = R.string.license_screen_name),
                        onClick = { navController.navigate(LICENSE_SCREEN_ROUTE) }
                    )
                    RaysIconButton(
                        onClick = { openUpdateDialog = true },
                        imageVector = Icons.Default.Update,
                        contentDescription = stringResource(id = R.string.update_check)
                    )
                },
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        val otherWorksList = rememberOtherWorksList()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues + PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (windowSizeClass.isCompact) {
                item {
                    IconArea()
                }
                item {
                    TextArea()
                }
                item {
                    HelpArea(
                        openSponsorDialog = openSponsorDialog,
                        onTranslateClick = { openBrowser(WEBLATE_URL) },
                        onSponsorDialogVisibleChange = { openSponsorDialog = it }
                    )
                    ButtonArea()
                }
            } else {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconArea()
                            HelpArea(
                                openSponsorDialog = openSponsorDialog,
                                onTranslateClick = { openBrowser(WEBLATE_URL) },
                                onSponsorDialogVisibleChange = { openSponsorDialog = it }
                            )
                            ButtonArea()
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextArea(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.about_screen_other_works),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            itemsIndexed(items = otherWorksList) { _, item ->
                OtherWorksItem(data = item)
            }
        }

        if (openUpdateDialog) {
            UpdateDialog(onClosed = { openUpdateDialog = false })
        }
    }
}

@Composable
private fun IconArea() {
    Image(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.4f)
            .aspectRatio(1f),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        painter = painterResource(id = R.drawable.ic_rays),
        contentDescription = null
    )
}

@Composable
private fun TextArea(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                Badge {
                    val badgeNumber = rememberSaveable { CommonUtil.getAppVersionName() }
                    Text(
                        text = badgeNumber,
                        modifier = Modifier.semantics { contentDescription = badgeNumber }
                    )
                }
            }
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(id = R.string.about_screen_app_full_name),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.padding(top = 16.dp),
            shape = RoundedCornerShape(10)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.about_screen_description_1),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.about_screen_description_2),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.about_screen_description_3),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HelpArea(
    openSponsorDialog: Boolean,
    onTranslateClick: () -> Unit,
    onSponsorDialogVisibleChange: (Boolean) -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Button(
            onClick = onTranslateClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Icon(imageVector = Icons.Default.Translate, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(id = R.string.help_translate), textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = { onSponsorDialogVisibleChange(true) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Icon(imageVector = Icons.Default.Coffee, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(id = R.string.sponsor), textAlign = TextAlign.Center)
        }
    }
    SponsorDialog(visible = openSponsorDialog, onClose = { onSponsorDialogVisibleChange(false) })
}

@Composable
private fun SponsorDialog(visible: Boolean, onClose: () -> Unit) {
    RaysDialog(
        visible = visible,
        onDismissRequest = onClose,
        icon = { Icon(imageVector = Icons.Default.Coffee, contentDescription = null) },
        title = { Text(text = stringResource(id = R.string.sponsor)) },
        selectable = false,
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = stringResource(id = R.string.sponsor_description))
                Spacer(modifier = Modifier.height(6.dp))
                ListItem(
                    modifier = Modifier.clickable {
                        openBrowser("https://afdian.net/a/SkyD666")
                        onClose()
                    },
                    headlineContent = { Text(text = stringResource(R.string.sponsor_afadian)) },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null)
                    }
                )
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.clickable {
                        openBrowser("https://www.buymeacoffee.com/SkyD666")
                        onClose()
                    },
                    headlineContent = { Text(text = stringResource(R.string.sponsor_buy_me_a_coffee)) },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Coffee, contentDescription = null)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(R.string.dialog_close))
            }
        },
    )
}

@Composable
private fun ButtonArea() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val boxModifier = Modifier.padding(vertical = 16.dp, horizontal = 6.dp)
        Box(
            modifier = boxModifier.background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CurlyCornerShape(
                    amp = with(LocalDensity.current) { 1.5.dp.toPx() },
                    count = 10
                ),
            ),
            contentAlignment = Alignment.Center
        ) {
            RaysIconButton(
                painter = painterResource(id = R.drawable.ic_github_24),
                contentDescription = stringResource(id = R.string.about_screen_goto_github_repo),
                onClick = { openBrowser(GITHUB_REPO) }
            )
        }
        Box(
            modifier = boxModifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = SquircleShape,
            ),
            contentAlignment = Alignment.Center
        ) {
            RaysIconButton(
                painter = painterResource(id = R.drawable.ic_telegram_24),
                contentDescription = stringResource(id = R.string.about_screen_join_telegram),
                onClick = { openBrowser("https://t.me/SkyD666Chat") }
            )
        }
        Box(
            modifier = boxModifier.background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CloverShape,
            ),
            contentAlignment = Alignment.Center
        ) {
            RaysIconButton(
                painter = painterResource(id = R.drawable.ic_discord_24),
                contentDescription = stringResource(id = R.string.about_screen_join_discord),
                onClick = { openBrowser("https://discord.gg/pEWEjeJTa3") }
            )
        }
    }
}

@Composable
private fun rememberOtherWorksList(): List<OtherWorksBean> {
    val context = LocalContext.current
    return remember {
        listOf(
            OtherWorksBean(
                name = context.getString(R.string.about_screen_other_works_raca_name),
                icon = R.drawable.ic_raca,
                description = context.getString(R.string.about_screen_other_works_raca_description),
                url = RACA_ANDROID_URL,
            ),
            OtherWorksBean(
                name = context.getString(R.string.about_screen_other_works_night_screen_name),
                icon = R.drawable.ic_night_screen,
                description = context.getString(R.string.about_screen_other_works_night_screen_description),
                url = NIGHT_SCREEN_URL,
            ),
        )
    }
}

@Composable
private fun OtherWorksItem(
    modifier: Modifier = Modifier,
    data: OtherWorksBean,
) {
    Card(
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { openBrowser(data.url) }
                .padding(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    modifier = Modifier
                        .size(30.dp)
                        .aspectRatio(1f),
                    model = data.icon,
                    contentDescription = data.name
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = data.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}