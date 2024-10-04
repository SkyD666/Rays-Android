//package com.skyd.rays.ui.activity
//
//import android.os.Bundle
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.WindowInsetsSides
//import androidx.compose.foundation.layout.imePadding
//import androidx.compose.foundation.layout.only
//import androidx.compose.foundation.layout.systemBars
//import androidx.compose.foundation.layout.windowInsetsPadding
//import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.ArrowUpward
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.TextRange
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import com.skyd.rays.R
//import com.skyd.rays.base.BaseComposeActivity
//import com.skyd.rays.model.preference.search.QueryPreference
//import com.skyd.rays.ui.component.BackIcon
//import com.skyd.rays.ui.component.RaysFloatingActionButton
//import com.skyd.rays.ui.local.LocalWindowSizeClass
//import com.skyd.rays.ui.screen.search.SearchBarInputField
//import com.skyd.rays.ui.screen.search.TrailingIcon
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class ImagePickerActivity : BaseComposeActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentBase {
//            ImagePicker()
//        }
//    }
//}
//
//@Composable
//private fun ImagePicker() {
//    val context = LocalContext.current
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//    val searchResultListState = rememberLazyStaggeredGridState()
//    val windowSizeClass = LocalWindowSizeClass.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//    var fabHeight by remember { mutableStateOf(0.dp) }
//    Scaffold(
//        modifier = Modifier.imePadding(),
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
//        floatingActionButton = {
//            AnimatedVisibility(
//                visible = remember {
//                    derivedStateOf { searchResultListState.firstVisibleItemIndex > 2 }
//                }.value
//            ) {
//                RaysFloatingActionButton(
//                    onClick = { scope.launch { searchResultListState.animateScrollToItem(0) } },
//                    onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
//                    contentDescription = stringResource(R.string.home_screen_search_result_list_to_top),
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.ArrowUpward,
//                        contentDescription = null
//                    )
//                }
//            }
//        },
//        topBar = {
//            LaunchedEffect(searchFieldValueState.text) {
//                delay(60)
//                QueryPreference.put(context, scope, searchFieldValueState.text)
//            }
//            SearchBarInputField(
//                modifier = Modifier.windowInsetsPadding(
//                    WindowInsets.systemBars
//                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
//                ),
//                onQueryChange = { searchFieldValueState = it },
//                query = searchFieldValueState,
//                onSearch = { state ->
//                    keyboardController?.hide()
//                    searchFieldValueState = state
//                },
//                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
//                leadingIcon = { BackIcon() },
//                trailingIcon = {
//                    TrailingIcon(showClearButton = searchFieldValueState.text.isNotEmpty()) {
//                        searchFieldValueState = TextFieldValue(
//                            text = QueryPreference.default,
//                            selection = TextRange(QueryPreference.default.length)
//                        )
//                    }
//                }
//            )
//        }
//    ) { innerPaddings ->
//    }
//}