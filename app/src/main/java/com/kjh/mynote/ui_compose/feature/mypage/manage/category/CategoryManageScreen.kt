package com.kjh.mynote.ui_compose.feature.mypage.manage.category

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun CategoryManageRoute(
    viewModel: CategoryManageComposeViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::handleEvent
    )

    when (val state = uiState.dialogState) {
        is DialogState.Add -> {
            AddCategoryDialog(
                dialogState = state,
                onEvent = viewModel::handleEvent
            )
        }
        is DialogState.Edit -> {
            EditCategoryDialog(
                dialogState = state,
                onEvent = viewModel::handleEvent
            )
        }
        is DialogState.Delete -> {
            DeleteCategoryDialog(
                dialogState = state,
                onEvent = viewModel::handleEvent
            )
        }
        DialogState.Hidden -> {}
    }
}
@Composable
fun CategoryManageScreen(
    modifier: Modifier = Modifier,
    uiState: CategoryManageUiState,
    onBackClick: () -> Unit,
    onEvent: (CategoryManageEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            MyToolBarCompose(
                title = stringResource(R.string.title_manage_category),
                onBackButtonClick = onBackClick,
                rightFirstImageRes = R.drawable.ic_add_24,
                rightFirstImageDesc = "Add Category",
                rightFirstImageClick = { onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Add(""))) }
            )

            CategoryList(
                list = uiState.categories,
                onClickEdit = { categoryItem ->
                    onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Edit(categoryItem))) },
                onClickDelete = { categoryItem ->
                    onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Delete(categoryItem)))
                }
            )
        }

        if (uiState.isLoading) {
            MyCircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}


@Composable
fun CategoryList(
    modifier: Modifier = Modifier,
    list: List<CategoryUiModel>,
    onClickEdit: (CategoryUiModel) -> Unit,
    onClickDelete: (CategoryUiModel) -> Unit
) {
    LazyColumn(modifier = modifier.padding(top = 20.dp)) {
        items(list, key = { it.id }) { item ->
            CategoryListItem(
                item = item,
                onClickEdit = onClickEdit,
                onClickDelete = onClickDelete
            )
        }
    }
}

@Composable
fun CategoryListItem(
    item: CategoryUiModel,
    onClickEdit: (CategoryUiModel) -> Unit,
    onClickDelete: (CategoryUiModel) -> Unit
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
            color = colorResource(R.color.black_900)
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = { onClickEdit(item) }
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit"
            )
        }
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = { onClickDelete(item) }
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete"
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = colorResource(R.color.black_50)
    )
}


@Composable
fun AddCategoryDialog(
    dialogState: DialogState.Add,
    onEvent: (CategoryManageEvent) -> Unit
) {
    MyTextFieldDialog(
        title = stringResource(R.string.title_add_category),
        value = dialogState.categoryName,
        placeHolder = stringResource(R.string.title_input_category_for_add),
        onValueChanged = {
            onEvent(CategoryManageEvent.UpdateDialogState(dialogState.copy(categoryName = it))) },
        confirmBtnText = stringResource(R.string.do_add),
        cancelBtnText = stringResource(R.string.do_cancel),
        onClickConfirm = { onEvent(CategoryManageEvent.AddCategory(dialogState.categoryName)) },
        onClickCancel = { onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden)) }
    )
}

@Composable
fun EditCategoryDialog(
    dialogState: DialogState.Edit,
    onEvent: (CategoryManageEvent) -> Unit
) {
    MyTextFieldDialog(
        title = stringResource(R.string.title_edit_category),
        value = dialogState.category.categoryName,
        desc = stringResource(R.string.desc_when_edit_category_name_change_same_category_notes),
        descFontColor = colorResource(R.color.red_500),
        placeHolder = stringResource(R.string.title_input_category_for_add),
        onValueChanged = {
            val updatedDialogState = dialogState.copy(
                category = dialogState.category.copy(categoryName = it)
            )
            onEvent(CategoryManageEvent.UpdateDialogState(updatedDialogState))
        },
        confirmBtnText = stringResource(R.string.do_modify),
        cancelBtnText = stringResource(R.string.do_cancel),
        onClickConfirm = { onEvent(CategoryManageEvent.EditCategory(dialogState.category)) },
        onClickCancel = { onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden)) }
    )
}

@Composable
fun DeleteCategoryDialog(
    dialogState: DialogState.Delete,
    onEvent: (CategoryManageEvent) -> Unit
) {
    MyDefaultDialog(
        title = stringResource(R.string.title_will_you_delete_category),
        desc = stringResource(R.string.desc_when_delete_category_change_same_category_notes),
        descFontColor = R.color.red_500,
        confirmButtonText = stringResource(R.string.yes_i_will_delete),
        cancelButtonText = stringResource(R.string.cancel),
        onClickConfirm = { onEvent(CategoryManageEvent.DeleteCategory(dialogState.category.id)) },
        onClickCancel = { onEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden)) }
    )
}