package com.kjh.mynote.ui_compose.feature.mypage.manage.category

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui_compose.components.MyCircularProgressIndicator
import com.kjh.mynote.ui_compose.components.MyDefaultDialog
import com.kjh.mynote.ui_compose.components.MyTextFieldDialog
import com.kjh.mynote.ui_compose.components.MyToolBarCompose
import com.kjh.mynote.ui_compose.theme.Black50
import com.kjh.mynote.ui_compose.theme.Black900
import com.kjh.mynote.ui_compose.theme.Red500

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun CategoryManageRoute(
    modifier: Modifier = Modifier,
    viewModel: CategoryManageComposeViewModel = hiltViewModel(),
    navigateUp: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CategoryManageSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    CategoryManageScreen(
        modifier = modifier,
        isLoading = uiState.isLoading,
        categoryItems = uiState.categories,
        onBackClick = navigateUp,
        onAddClick = {
            viewModel.handleEvent(
                CategoryManageEvent.UpdateDialogState(
                    CategoryManageDialogState.Add()
                )
            )
        },
        onEditClick = { categoryItem ->
            viewModel.handleEvent(
                CategoryManageEvent.UpdateDialogState(
                    CategoryManageDialogState.Edit(categoryItem)
                )
            )
        },
        onDeleteClick = { categoryId ->
            viewModel.handleEvent(
                CategoryManageEvent.UpdateDialogState(
                    CategoryManageDialogState.Delete(categoryId)
                )
            )
        }
    )

    when (val dialogState = uiState.manageDialogState) {
        is CategoryManageDialogState.Add -> {
            MyTextFieldDialog(
                titleRes = R.string.title_add_category,
                value = dialogState.categoryName,
                placeHolderRes = R.string.title_input_category_for_add,
                confirmBtnTextRes = R.string.do_add,
                cancelBtnTextRes = R.string.do_cancel,
                onValueChanged = { newValue ->
                    viewModel.handleEvent(
                        CategoryManageEvent.UpdateDialogState(
                            CategoryManageDialogState.Add(newValue)
                        )
                    )
                },
                onClickConfirm = {
                    viewModel.handleEvent(CategoryManageEvent.AddCategory(dialogState.categoryName))
                },
                onClickCancel = {
                    viewModel.handleEvent(
                        CategoryManageEvent.UpdateDialogState(
                            CategoryManageDialogState.Hidden
                        )
                    )
                }
            )
        }
        is CategoryManageDialogState.Edit -> {
            MyTextFieldDialog(
                titleRes = R.string.title_edit_category,
                value = dialogState.categoryItem.categoryName,
                descRes = R.string.desc_when_edit_category_name_change_same_category_notes,
                descFontColor = Red500,
                placeHolderRes = R.string.title_input_category_for_add,
                confirmBtnTextRes = R.string.do_modify,
                cancelBtnTextRes = R.string.do_cancel,
                onValueChanged = {
                    val updatedCategoryItem = dialogState.categoryItem.copy(categoryName = it)
                    viewModel.handleEvent(
                        CategoryManageEvent.UpdateDialogState(
                            CategoryManageDialogState.Edit(updatedCategoryItem)
                        )
                    )
                },
                onClickConfirm = {
                    viewModel.handleEvent(CategoryManageEvent.EditCategory(dialogState.categoryItem))
                },
                onClickCancel = {
                    viewModel.handleEvent(
                        CategoryManageEvent.UpdateDialogState(
                            CategoryManageDialogState.Hidden
                        )
                    )
                }
            )
        }
        is CategoryManageDialogState.Delete -> {
            MyDefaultDialog(
                titleRes = R.string.title_will_you_delete_category,
                descRes = R.string.desc_when_delete_category_change_same_category_notes,
                descFontColor = Red500,
                confirmButtonTextRes = R.string.yes_i_will_delete,
                cancelButtonTextRes = R.string.cancel,
                onClickConfirm = {
                    viewModel.handleEvent(
                        CategoryManageEvent.DeleteCategory(dialogState.categoryId)
                    )
                },
                onClickCancel = {
                    viewModel.handleEvent(
                        CategoryManageEvent.UpdateDialogState(
                            CategoryManageDialogState.Hidden
                        )
                    )
                }
            )
        }
        is CategoryManageDialogState.Hidden -> {}
    }
}

@Composable
fun CategoryManageScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    categoryItems: List<CategoryUiModel>,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (CategoryUiModel) -> Unit,
    onDeleteClick: (Int) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MyToolBarCompose(
                titleRes = R.string.title_manage_category,
                onBackButtonClick = onBackClick,
                rightFirstImageRes = R.drawable.ic_add_24,
                rightFirstImageDesc = "Add Category",
                rightFirstImageClick = onAddClick
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            CategoryList(
                modifier = modifier,
                categoryItems = categoryItems,
                onClickEdit = onEditClick,
                onClickDelete = onDeleteClick
            )

            if (isLoading) {
                MyCircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
fun CategoryList(
    modifier: Modifier = Modifier,
    categoryItems: List<CategoryUiModel>,
    onClickEdit: (CategoryUiModel) -> Unit,
    onClickDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        items(categoryItems, key = { it.id }) { item ->
            CategoryListItem(
                item = item,
                onClickEdit = { onClickEdit(item) },
                onClickDelete = { onClickDelete(item.id) }
            )
        }
    }
}

@Composable
fun CategoryListItem(
    item: CategoryUiModel,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.categoryName,
            fontSize = 16.sp,
            color = Black900
        )

        if (!item.isDefaultCategory()) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onClickEdit
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit"
                )
            }
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onClickDelete
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = Black50
    )
}