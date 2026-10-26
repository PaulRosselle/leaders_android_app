package com.leaders.app.activities.puzzle;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.PuzzleSource;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.PuzzleExportUtils;
import com.leaders.app.utilities.PuzzleImportUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.puzzle.PuzzleDetailsView;
import com.leaders.app.views.puzzle.PuzzleSelectorGroupView;
import com.leaders.app.views.puzzle.SearchToolbarView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleIdentity;
import com.leaders.puzzlelogic.entities.PuzzleImportAnalysis;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleCategory;
import com.leaders.puzzlelogic.search.PuzzleSaveCriteria;
import com.leaders.puzzlelogic.search.PuzzleSaveListViewModel;
import com.leaders.puzzlelogic.search.PuzzleSaveToolbarMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PuzzleMenuActivity extends BaseActivity implements SearchToolbarView.SearchToolbarListener {
    private ActivityResultLauncher<String> importPuzzleFileSelector;
    private ActivityResultLauncher<Uri> exportPuzzleDirectorySelector;

    private enum PuzzleSelectionAction {
        Add,
        EditPuzzle,
        ChangeDetails,
        Remove,
        Copy,
        Import,
        Export,
        SelectAll,
        UnselectAll;

        private int getIconResId() {
            switch (this) {
                case Add: return R.drawable.icon_add;
                case EditPuzzle: return R.drawable.icon_puzzle;
                case ChangeDetails: return R.drawable.icon_edit;
                case Remove: return R.drawable.icon_remove;
                case Copy: return R.drawable.icon_copy;
                case Import: return R.drawable.icon_import;
                case Export: return R.drawable.icon_export;
                case SelectAll: return R.drawable.icon_select_all;
                case UnselectAll: return R.drawable.icon_unselect_all;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case Add: return R.string.new_puzzle;
                case EditPuzzle: return R.string.edit_puzzle;
                case ChangeDetails: return R.string.change_details;
                case Remove: return R.string.remove;
                case Copy: return R.string.copy;
                case Import: return R.string.import_puzzle;
                case Export: return R.string.export_puzzle;
                case SelectAll: return R.string.select_all;
                case UnselectAll: return R.string.unselect_all;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(PuzzleMenuActivity activity) {
            switch (this) {
                case Add: return activity::onNewPuzzleClick;
                case EditPuzzle: return activity::onEditPuzzleClick;
                case ChangeDetails: return activity::onChangeDetailsClick;
                case Remove: return activity::onRemoveClick;
                case Copy: return activity::onCopyClick;
                case Import: return activity::onImportClick;
                case Export: return activity::onExportClick;
                case SelectAll: return activity::onSelectAllClick;
                case UnselectAll: return activity::onUnselectAllClick;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private View vwDialogBg;
    private MaterialButton btnPuzzleActions;
    private ActionsMenuView amvPuzzleActions;
    private PuzzleDetailsView pdvDetails;
    private MaterialButtonToggleGroup mbtgPuzzlesCategory;
    private PuzzleSelectorGroupView psgvPuzzles;
    private PuzzleCategory puzzlesCategory;

    private PuzzleSaveListViewModel searchViewModel;
    private SearchToolbarView stbSearch;

    private List<OfficialPuzzleSave> officialPuzzleSaves;
    private List<CustomPuzzleSave> customPuzzleSaves;

    @Override
    protected void initViews() {
        super.initViews();

        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzleMenu);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleMenu);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzleMenu);
        pdvDetails = findViewById(R.id.pdvDetails_actPuzzleMenu);
        mbtgPuzzlesCategory = findViewById(R.id.mbtgPuzzlesCategory_actPuzzleMenu);
        psgvPuzzles = findViewById(R.id.psgvPuzzles_actPuzzleMenu);

        searchViewModel = new ViewModelProvider(this).get(PuzzleSaveListViewModel.class);
        stbSearch = findViewById(R.id.stbSearch_actPuzzleMenu);

        for (PuzzleSelectionAction action : PuzzleSelectionAction.values()) {
            amvPuzzleActions.addActionButton(
                    action.getIconResId(),
                    action.getTextResId(),
                    action.ordinal(),
                    action.getOnClickListener(this)
            );
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        vwDialogBg.setOnClickListener(this::onDialogBgClick);
        btnPuzzleActions.setOnClickListener(this::btnPuzzleActionsClick);
        psgvPuzzles.setSelectionChangeListener(this::onPuzzleSelectionChange);
        mbtgPuzzlesCategory.addOnButtonCheckedListener(this::onPuzzleCategoryChange);
        pdvDetails.setOnBtnCancelClick(this::onDetailsCancel);
        pdvDetails.setOnBtnSaveClick(this::onDetailsSave);

        searchViewModel.getSourceSaves().observe(this, saves ->
                stbSearch.setFilterGroups(PuzzleSaveToolbarMapper.buildFilterGroups(this, saves))
        );
        searchViewModel.getDisplayedSaves().observe(this, saves ->
                psgvPuzzles.setPuzzles(saves)
        );
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        importPuzzleFileSelector = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importFromFile(uri);
                    }
                });

        exportPuzzleDirectorySelector = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        exportToFile(uri);
                    }
                });

        officialPuzzleSaves = JsonUtils.loadOfficialPuzzles(this);
        customPuzzleSaves = JsonUtils.loadCustomPuzzles(this);

        initToolbar();

        // This will cause "onPuzzleSelectionChange" to be called
        ((MaterialButton) (findViewById(R.id.btnOfficial_actPuzzleMenu))).setChecked(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_menu;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleMenu;
    }

    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleMenu;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PuzzleMenu;
    }

    private List<? extends PuzzleSave> getPuzzlesFromCategory() {
        return puzzlesCategory == PuzzleCategory.Official ? officialPuzzleSaves : customPuzzleSaves;
    }

    private void onPuzzleCategoryChange(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (!isChecked) {
            return;
        }

        puzzlesCategory = checkedId == R.id.btnOfficial_actPuzzleMenu ?
                PuzzleCategory.Official : PuzzleCategory.Custom;

        searchViewModel.setCategory(puzzlesCategory);
        stbSearch.setSortMenu(
                getSortMenuResId(),
                PuzzleSaveToolbarMapper.toMenuItemId(searchViewModel.getCriteria().getSortMode())
        );
        refreshPuzzles();
    }

    private void onPuzzleSelectionChange() {
        // A click on a puzzle outside of selection mode starts the PuzzlePlayer
        List<PuzzleSave> selectedPuzzles = psgvPuzzles.getSelectedPuzzles();
        if (!psgvPuzzles.isSingleSelection() || selectedPuzzles.size() != 1) {
            return;
        }

        Intent intent = ActivityType.PuzzlePlayer.getIntent(this);

        PuzzleSource puzzleSource = puzzlesCategory == PuzzleCategory.Official ?
                PuzzleSource.OfficialSelection : PuzzleSource.CustomSelection;
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_SOURCE, puzzleSource.name());

        PuzzleSave selectedPuzzle = selectedPuzzles.get(0);
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, getPuzzlesFromCategory().indexOf(selectedPuzzle));

        goToActivity(intent);
    }

    //region TOOLBAR METHODS

    private void initToolbar() {
        // Restore the state before the listener is applied to avoid ealy notifications
        PuzzleSaveCriteria criteria = searchViewModel.getCriteria();
        stbSearch.setSelectedFilters(PuzzleSaveToolbarMapper.toSelection(criteria.getFilters()));
        stbSearch.setSearchKey(criteria.getSearchKey());
        stbSearch.setListener(this);
    }

    private void refreshPuzzles() {
        searchViewModel.setSaves(getPuzzlesFromCategory());
    }

    @Override
    public void onSearchKeyChanged(@NonNull String searchKey) {
        searchViewModel.setSearchKey(searchKey);
    }

    @Override
    public void onSortChanged(int sortItemId) {
        searchViewModel.setSortMode(PuzzleSaveToolbarMapper.toSortMode(sortItemId));
    }

    @Override
    public void onFiltersChanged(@NonNull Map<String, Set<String>> selection) {
        searchViewModel.setFilters(PuzzleSaveToolbarMapper.toFilters(selection));
    }

    @MenuRes
    private int getSortMenuResId() {
        return puzzlesCategory == PuzzleCategory.Official ? R.menu.menu_sort_official : R.menu.menu_sort_custom;
    }

    //endregion

    //region PUZZLE ACTIONS METHODS

    private void hidePuzzleActions() {
        amvPuzzleActions.setVisibility(View.GONE);
        vwDialogBg.setVisibility(View.GONE);
    }

    private void btnPuzzleActionsClick(View v) {
        int puzzlesCount = getPuzzlesFromCategory().size();
        int selectedPuzzlesCount = psgvPuzzles.getSelectedPuzzles().size();

        // Before showing the actions menu, we must update the available actions based on the puzzle selection
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.EditPuzzle.ordinal(),
                selectedPuzzlesCount == 1 && puzzlesCategory == PuzzleCategory.Custom);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.ChangeDetails.ordinal(),
                selectedPuzzlesCount == 1 && puzzlesCategory == PuzzleCategory.Custom);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Remove.ordinal(),
                selectedPuzzlesCount > 0 && puzzlesCategory == PuzzleCategory.Custom);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Copy.ordinal(),
                selectedPuzzlesCount > 0);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Export.ordinal(),
                selectedPuzzlesCount > 0);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.SelectAll.ordinal(),
                selectedPuzzlesCount < puzzlesCount);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.UnselectAll.ordinal(),
                selectedPuzzlesCount > 0);

        amvPuzzleActions.setVisibility(View.VISIBLE);
        vwDialogBg.setVisibility(View.VISIBLE);
    }

    private void onNewPuzzleClick(View v) {
        goToActivity(ActivityType.PuzzleEditor);
    }

    private void onEditPuzzleClick(View v) {
        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        int puzzleIdx = customPuzzleSaves.indexOf((CustomPuzzleSave) psgvPuzzles.getSelectedPuzzles().get(0));
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);
        goToActivity(intent);
    }

    private void onChangeDetailsClick(View v) {
        PuzzleSave puzzleSave = psgvPuzzles.getSelectedPuzzles().get(0);

        pdvDetails.setDefaultPuzzleName(puzzleSave.getName());
        pdvDetails.setDefaultPuzzleAuthor(puzzleSave.getAuthor());
        pdvDetails.setPuzzleDifficulty(puzzleSave.getDifficulty());
        pdvDetails.setVisibility(View.VISIBLE);

        amvPuzzleActions.setVisibility(View.GONE);
    }

    private void onRemoveClick(View v) {
        if (puzzlesCategory == PuzzleCategory.Official) {
            throw new IllegalStateException("Official puzzle removal is forbidden");
        }

        List<PuzzleSave> selectedPuzzles = psgvPuzzles.getSelectedPuzzles();

        String dialogTitle;
        if (selectedPuzzles.size() == 1) {
            dialogTitle = String.format(getString(R.string.remove_selected_puzzle), selectedPuzzles.get(0).getName());
        } else {
            dialogTitle = getString(R.string.remove_selected_puzzles);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(dialogTitle);
        builder.setMessage(getString(R.string.warning_removal_cannot_be_undone));
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            for (PuzzleSave puzzleSave : selectedPuzzles) {
                customPuzzleSaves.remove((CustomPuzzleSave) puzzleSave);
            }
            JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);
            refreshPuzzles();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        hidePuzzleActions();
    }

    private void onCopyClick(View v) {
        for (PuzzleSave puzzleSave : psgvPuzzles.getSelectedPuzzles()) {
            customPuzzleSaves.add(createCopy(puzzleSave));
        }

        JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);
        refreshPuzzles();

        hidePuzzleActions();
    }

    private CustomPuzzleSave createCopy(@NonNull PuzzleSave puzzleSave) {
        try {
            return new CustomPuzzleSave(
                    getAvailableCopyName(puzzleSave.getName(), customPuzzleSaves),
                    puzzleSave.getAuthor(),
                    puzzleSave.getLifetime(),
                    puzzleSave.getDifficulty(),
                    new JSONObject(puzzleSave.getDatas().toString()),
                    false
            );
        } catch (JSONException e) {
            throw new IllegalStateException("Error while parsing puzzle datas", e);
        }
    }

    @NonNull
    private static String getAvailableCopyName(@NonNull String originalName,
                                               @NonNull List<? extends PuzzleSave> existingSaves) {
        Set<String> existingNames = new HashSet<>();

        String prefix = originalName + " (";

        for (PuzzleSave save : existingSaves) {
            String name = save.getName();

            if (name.startsWith(prefix)) {
                existingNames.add(name);
            }
        }

        int index = 1;
        String candidate;

        do {
            candidate = prefix + index + ")";
            index++;
        } while (existingNames.contains(candidate));

        return candidate;
    }

    private void onImportClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.import_puzzle_title);

        builder.setItems(
                new String[]{
                        getString(R.string.import_option_lbe_link_from_clipboard),
                        getString(R.string.import_option_from_file)
                },
                (dialog, which) -> {
                    switch (which) {
                        case 0: importLbeLinkFromClipboard();break;
                        case 1: selectImportFile();break;
                    }
                });
        builder.show();

        hidePuzzleActions();
    }

    private void selectImportFile() {
        importPuzzleFileSelector.launch("application/json");
    }

    private void importFromFile(@NonNull Uri uri) {
        try {
            // First we try to load the puzzles from the file
            List<CustomPuzzleSave> importedPuzzles = JsonUtils.loadCustomPuzzlesFromFile(this, uri);

            PuzzleImportAnalysis importAnalysis = new PuzzleImportAnalysis(importedPuzzles, customPuzzleSaves);

            if (!importAnalysis.getNewPuzzles().isEmpty()) {
                // Then we save the new custom puzzles list
                customPuzzleSaves.addAll(importAnalysis.getNewPuzzles());
                JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);

                // Finally we display the new puzzles and inform the user that the import was successful
                refreshPuzzles();

                Toast.makeText(this, R.string.import_successful, Toast.LENGTH_SHORT).show();
            }

            if (!importAnalysis.getDuplicates().isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<PuzzleIdentity, String> entry : importAnalysis.getDuplicates().entrySet()) {
                    stringBuilder.append(String.format(getString(R.string.duplicate_of),
                            entry.getKey().getName().toUpperCase(),
                            entry.getValue().toUpperCase()
                    ));
                }

                new AlertDialog.Builder(this, R.style.alert_dialog_theme)
                        .setTitle(R.string.puzzles_not_imported)
                        .setMessage(stringBuilder.toString().trim())
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, R.string.error_import_file_loading, Toast.LENGTH_SHORT).show();
        }
    }

    private void importLbeLinkFromClipboard() {
        // The import method handle itself error messages
        GameHistory gameHistory = PuzzleImportUtils.importPuzzleFromClipboard(this);
        if (gameHistory == null) {
            return;
        }

        CustomPuzzleSave importedPuzzleSave = CustomPuzzleSave.getDefault(gameHistory);
        JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);

        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        int puzzleIdx = customPuzzleSaves.indexOf(importedPuzzleSave);
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_IMPORTED, true);
        goToActivity(intent);
    }

    private void onExportClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.export_puzzle_title);
        builder.setItems(
                new String[]{
                        getString(R.string.export_option_lbe_link_in_clipboard),
                        getString(R.string.export_option_to_file)
                },
                (dialog, which) -> {
                    switch (which) {
                        case 0: exportToLbeLinks();break;
                        case 1: selectExportFile();break;
                    }
                });
        builder.show();

        hidePuzzleActions();
    }

    private void selectExportFile() {
        exportPuzzleDirectorySelector.launch(null);
    }
    private void exportToFile(@NonNull Uri directoryUri) {
        // Now that the user has chosen a directory to export the puzzles
        // we can set up an alertDialog with an editText so the user can
        // input the file name
        DocumentFile fileDirectory = DocumentFile.fromTreeUri(this, directoryUri);

        if (fileDirectory == null || !fileDirectory.canWrite()) {
            Toast.makeText(this, R.string.error_export_file_writing, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);

        builder.setTitle(R.string.choose_file_name);

        final EditText edtFileName = new EditText(this);
        edtFileName.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(edtFileName);

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            // If the file already exists, it will be renamed with an incremented value at the end
            String fileName = edtFileName.getText().toString();

            DocumentFile file = fileDirectory.createFile("application/json", fileName);

            if (file == null) {
                Toast.makeText(this, R.string.error_export_file_creation, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonUtils.savePuzzlesToFile(this, psgvPuzzles.getSelectedPuzzles(), file.getUri());

            Toast.makeText(this, R.string.export_successful, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void exportToLbeLinks() {
        StringBuilder builder = new StringBuilder();
        for (PuzzleSave puzzleSave : psgvPuzzles.getSelectedPuzzles()) {
            builder.append(puzzleSave.getName());
            if (!puzzleSave.getAuthor().isEmpty()) {
                builder.append(String.format(getString(R.string.by_author), puzzleSave.getAuthor()));
            }
            builder.append("\n");
            builder.append(PuzzleExportUtils.getLbeUrl(puzzleSave));
            builder.append("\n\n");
        }

        PuzzleExportUtils.exportAsTextIntent(this, builder.toString().trim());
    }

    private void onSelectAllClick(View v) {
        psgvPuzzles.selectAllPuzzles();
        hidePuzzleActions();
    }

    private void onUnselectAllClick(View v) {
        psgvPuzzles.clearPuzzleSelection();
        hidePuzzleActions();
    }

    private void onDialogBgClick(View v) {
        if (amvPuzzleActions.getVisibility() == View.VISIBLE) {
            hidePuzzleActions();
        } else if (pdvDetails.getVisibility() == View.VISIBLE) {
            hidePuzzleDetails();
        }
    }

    //endregion

    //region PUZZLE DETAILS

    private void onDetailsCancel(View v) {
        hidePuzzleDetails();
    }

    private void onDetailsSave(View v) {
        CustomPuzzleSave customPuzzleSave = (CustomPuzzleSave) psgvPuzzles.getSelectedPuzzles().get(0);

        customPuzzleSave.setName(pdvDetails.getPuzzleName());
        customPuzzleSave.setAuthor(pdvDetails.getPuzzleAuthor());
        customPuzzleSave.setDifficulty(pdvDetails.getPuzzleDifficulty());

        JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);
        Toast.makeText(this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
        refreshPuzzles();

        hidePuzzleDetails();
    }

    private void hidePuzzleDetails() {
        pdvDetails.setVisibility(View.GONE);
        vwDialogBg.setVisibility(View.GONE);
    }

    //endregion
}