/*!
 * page:dictionaries js-script library.
 */

function drawDictionariesPage() {
    getDictionaries(function (dictionaries) {
        displayPage('dictionaries');

        initTableListeners('dictionaries', resetDictionarySelection);

        $('#dictionaries-table-row').css('height', calcInitTableHeight());

        $('#dictionaries-btn-run').off().on('click', function () {
            drawRunPage(dictionaries)
        });
        $('#dictionaries-btn-cards').off().on('click', function () {
            const selectedDictionaries = findSelectedDictionaries(dictionaries);
            if (selectedDictionaries.length === 1) {
                selectedDictionary = selectedDictionaries[0];
                drawDictionaryCardsPage();
            }
        });
        $('#dictionaries-btn-upload').off().on('click', () => {
            $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
        }).on('change', (e) => {
            const file = e.target.files[0];
            if (file !== undefined) {
                uploadDictionaryFile(file);
            }
        });
        $('#dictionaries-btn-download').off().on('click', function (e) {
            e.preventDefault();
            const options = $('#dictionaries-btn-download-options')
            options.toggleClass('show');
            options.css({
                display: 'block',
                position: 'absolute',
                transform: 'translate3d(0, 38px, 0)',
                top: 0,
                left: 0
            });
        });
        $(document).click(function (event) {
            if (!$(event.target).closest('#dictionaries-btn-download, #dictionaries-btn-download-options').length) {
                $('#dictionaries-btn-download-options').removeClass('show').hide();
            }
        });
        $('#dictionaries-btn-download-option-xml').off().on('click', function () {
            downloadDictionaryFile(dictionaries, 'xml');
            $('#dictionaries-btn-download-options').removeClass('show').hide();
        });
        $('#dictionaries-btn-download-option-json').off().on('click', function () {
            downloadDictionaryFile(dictionaries, 'json');
            $('#dictionaries-btn-download-options').removeClass('show').hide();
        });
        $('#dictionaries-btn-settings').off().on('click', function () {
            if (settings === undefined) {
                getSettings(function (res) {
                    settings = res;
                    fillSettingsDialog();
                });
            } else {
                fillSettingsDialog();
            }
        });

        bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-dictionary-prompt')).hide();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('add-dictionary-dialog')).hide();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('edit-dictionary-dialog')).hide();
        initDictionaryDeletePrompt();
        initAddDictionaryDialog();
        initEditDictionaryDialog(dictionaries);
        initSettingsDialog();

        const headers = $('#dictionaries-table-row th');
        const thName = $(headers[0]);
        const thSourceLang = $(headers[1]);
        const thTargetLang = $(headers[2]);
        const thTotal = $(headers[3]);
        const thLearned = $(headers[4]);
        thName.off('click').on('click', function () {
            const direction = sortDirection(thName);
            dictionaries.sort((a, b) => {
                const left = a.name;
                const right = b.name;
                return direction ? left.localeCompare(right) : right.localeCompare(left);
            });
            drawDictionariesTable(dictionaries);
        });
        thSourceLang.off('click').on('click', function () {
            const direction = sortDirection(thSourceLang);
            dictionaries.sort((a, b) => {
                const left = getLanguageNameByCode(a.sourceLang);
                const right = getLanguageNameByCode(b.sourceLang);
                return direction ? left.localeCompare(right) : right.localeCompare(left);
            });
            drawDictionariesTable(dictionaries);
        });
        thTargetLang.off('click').on('click', function () {
            const direction = sortDirection(thTargetLang);
            dictionaries.sort((a, b) => {
                const left = getLanguageNameByCode(a.targetLang);
                const right = getLanguageNameByCode(b.targetLang);
                return direction ? left.localeCompare(right) : right.localeCompare(left);
            });
            drawDictionariesTable(dictionaries);
        });
        thTotal.off('click').on('click', function () {
            const direction = sortDirection(thTotal);
            dictionaries.sort((a, b) => {
                const left = a.total;
                const right = b.total;
                return direction ? left - right : right - left;
            });
            drawDictionariesTable(dictionaries);
        });
        thLearned.off('click').on('click', function () {
            const direction = sortDirection(thLearned);
            dictionaries.sort((a, b) => {
                const left = a.learned;
                const right = b.learned;
                return direction ? left - right : right - left;
            });
            drawDictionariesTable(dictionaries);
        });

        drawDictionariesTable(dictionaries);
    });
}

function drawDictionariesTable(dictionaries) {
    const tbody = $('#dictionaries tbody');
    tbody.html('');

    $.each(dictionaries, function (index, dictionary) {
        let row = $(`<tr id="${'d' + dictionary.dictionaryId}">
                            <td>${dictionary.name}</td>
                            <td>${getLanguageNameByCode(dictionary.sourceLang)}</td>
                            <td>${getLanguageNameByCode(dictionary.targetLang)}</td>                            
                            <td>${dictionary.total}</td>
                            <td>${dictionary.learned}</td>
                          </tr>`);
        row.dblclick(function () {
            resetRowSelection(tbody);
            markRowSelected(row)
            drawRunPage(dictionaries)
        });
        tbody.append(row);
    });

    const rows = tbody.find('tr');
    let lastSelectedRow = null;
    rows.on('click', function (event) {
        if (!event.shiftKey) {
            resetRowSelection(tbody);
        }
        if (event.shiftKey && lastSelectedRow) {
            if (isRowSelected($(this))) {
                // to exclude rows from the result set
                markRowUnselected($(this))
                return
            }
            // multiple rows selected
            const start = lastSelectedRow.index();
            const end = $(this).index();
            const selectedRows = rows.slice(Math.min(start, end), Math.max(start, end) + 1)
            if (selectedRows.length > 1) {
                $('#dictionaries-btn-run').prop('disabled', false);
                toggleManageDictionariesPageButtons(true);
            }
            $.each(selectedRows, function (index, item) {
                markRowSelected($(item));
            })
        } else {
            // single row selected
            $('#dictionaries-btn-run').prop('disabled', false);
            toggleManageDictionariesPageButtons(false);
            markRowSelected($(this));
            onSelectDictionary(dictionaries);
            lastSelectedRow = $(this);
        }
    });
}

function uploadDictionaryFile(file) {
    const btnUpload = $('#dictionaries-btn-upload-label');
    const reader = new FileReader()
    const type = getFileExtensionType(file.name)
    reader.onload = function (e) {
        const txt = e.target.result;
        uploadDictionary(txt, drawDictionariesPage, type, function () {
            btnUpload.addClass('btn-outline-danger');
        });
    }
    reader.readAsArrayBuffer(file);
    $('#dictionaries-btn-upload').val('');
}

function downloadDictionaryFile(dictionaries, type) {
    const selectedDictionaries = findSelectedDictionaries(dictionaries);
    if (selectedDictionaries.length !== 1) {
        return;
    }
    const selectedDictionary = selectedDictionaries[0]
    const filename = toFilename(selectedDictionary.name) + '-' + new Date().toISOString().substring(0, 19) + '.' + type;
    downloadDictionary(selectedDictionary.dictionaryId, filename, type);
}

function initDictionaryDeletePrompt() {
    $('#delete-dictionary-prompt-confirm').off('click').on('click', function () {
        const body = $('#delete-dictionary-prompt-body');
        const id = body.attr('item-id');
        if (!id) {
            return;
        }
        deleteDictionary(id, drawDictionariesPage);
    });
}

function initAddDictionaryDialog() {
    const dialogId = 'add'
    $('#dictionary-btn-add').off('click').on('click', function () { // push open dialog
        onChangeDictionaryDialogMains(dialogId);
    });

    $('#add-dictionary-dialog-name').off('input').on('input', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#add-dictionary-dialog-source-lang').off('change').on('change', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#add-dictionary-dialog-target-lang').off('change').on('change', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    const numberOfRightAnswersInput = $('#add-dictionary-dialog-number-of-right-answers')
    numberOfRightAnswersInput.off('input').on('input', function () {
        onChangeDictionaryDialogMains(dialogId);
    });

    numberOfRightAnswersInput.val(defaultNumberOfRightAnswers);

    $('#add-dictionary-dialog-save').off('click').on('click', function () {
        const res = createResourceDictionaryEntity(dialogId)
        const onDone = function (id) {
            if (id === '') {
                id = res.dictionaryId;
            }
            drawDictionariesPage();
            scrollToRow('#d' + id, '#dictionaries-table-row', markRowSelected);
        };
        createDictionary(res, onDone);
    });
}

function initEditDictionaryDialog(dictionaries) {
    const dialogId = 'edit'
    $('#dictionary-btn-edit').off('click').on('click', function () { // push open dialog
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#edit-dictionary-dialog-name').off('input').on('input', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#edit-dictionary-dialog-number-of-right-answers').off('input').on('input', function () {
        onChangeDictionaryDialogMains(dialogId);
    });

    $('#edit-dictionary-dialog-source-lang').prop('disabled', true);
    $('#edit-dictionary-dialog-target-lang').prop('disabled', true);
    $('#edit-dictionary-dialog-save').off('click').on('click', function () {
        const id = $('#edit-dictionary-dialog').attr('item-id');
        if (id === undefined) {
            return;
        }
        const selectedDictionaries = findSelectedDictionaries(dictionaries);
        if (selectedDictionaries.length !== 1) {
            return;
        }
        const res = selectedDictionaries[0];
        res.name = $('#edit-dictionary-dialog-name').val();
        res.numberOfRightAnswers = $('#edit-dictionary-dialog-number-of-right-answers').val().trim();
        const onDone = function () {
            drawDictionariesPage();
            scrollToRow('#d' + id, '#dictionaries-table-row', markRowSelected);
        };
        updateDictionary(res, onDone);
    });
}

function initSettingsDialog() {
    const inputStageShowNumberOfWord = $('#input-stage-show-number-of-words');
    const inputStageOptionsNumberOfVariants = $('#input-stage-options-number-of-variants');
    const inputNumberOfWordsPerStage = $('#input-number-of-words-per-stage');
    const checkboxStageMosaicSrcToDst = $('#checkbox-stage-mosaic-src-to-dst');
    const checkboxStageOptionsSrcToDst = $('#checkbox-stage-options-src-to-dst');
    const checkboxStageWritingSrcToDst = $('#checkbox-stage-writing-src-to-dst');
    const checkboxStageSelfTestSrcToDst = $('#checkbox-stage-self-test-src-to-dst');
    const checkboxStageMosaicDstToSrc = $('#checkbox-stage-mosaic-dst-to-src');
    const checkboxStageOptionsDstToSrc = $('#checkbox-stage-options-dst-to-src');
    const checkboxStageWritingDstToSrc = $('#checkbox-stage-writing-dst-to-src');
    const checkboxStageSelfTestDstToSrc = $('#checkbox-stage-self-test-dst-to-src');

    const btnSave = $('#settings-modal-save-btn');

    function isValidUserSettings() {
        return isIntNumber(inputNumberOfWordsPerStage.val(), 2, 20) &&
            isIntNumber(inputStageShowNumberOfWord.val(), 2, 20) &&
            isIntNumber(inputStageOptionsNumberOfVariants.val(), 2, 15);
    }

    btnSave.prop('disabled', true);
    inputStageShowNumberOfWord.off('input').on('input', function () {
        btnSave.prop('disabled', !isValidUserSettings());
    });
    inputNumberOfWordsPerStage.off('input').on('input', function () {
        btnSave.prop('disabled', !isValidUserSettings());
    });
    inputStageOptionsNumberOfVariants.off('input').on('input', function () {
        btnSave.prop('disabled', !isValidUserSettings());
    });
    checkboxStageMosaicSrcToDst.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageMosaicSourceLangToTargetLang === checkboxStageMosaicSrcToDst.prop('checked'));
    });
    checkboxStageOptionsSrcToDst.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageOptionsSourceLangToTargetLang === checkboxStageOptionsSrcToDst.prop('checked'));
    });
    checkboxStageWritingSrcToDst.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageWritingSourceLangToTargetLang === checkboxStageWritingSrcToDst.prop('checked'));
    });
    checkboxStageSelfTestSrcToDst.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageSelfTestSourceLangToTargetLang === checkboxStageSelfTestSrcToDst.prop('checked'));
    });
    checkboxStageMosaicDstToSrc.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageMosaicTargetLangToSourceLang === checkboxStageMosaicDstToSrc.prop('checked'));
    });
    checkboxStageOptionsDstToSrc.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageOptionsTargetLangToSourceLang === checkboxStageOptionsDstToSrc.prop('checked'));
    });
    checkboxStageWritingDstToSrc.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageWritingTargetLangToSourceLang === checkboxStageWritingDstToSrc.prop('checked'));
    });
    checkboxStageSelfTestDstToSrc.off('change').on('change', function () {
        btnSave.prop('disabled', settings.stageSelfTestTargetLangToSourceLang === checkboxStageSelfTestDstToSrc.prop('checked'));
    });
    btnSave.off().on('click', function () {
        btnSave.prop('disabled', true);
        const _settings = {
            "stageShowNumberOfWords": inputStageShowNumberOfWord.val(),
            "stageOptionsNumberOfVariants": inputStageOptionsNumberOfVariants.val(),
            "numberOfWordsPerStage": inputNumberOfWordsPerStage.val(),
            "stageMosaicSourceLangToTargetLang": checkboxStageMosaicSrcToDst.prop('checked'),
            "stageOptionsSourceLangToTargetLang": checkboxStageOptionsSrcToDst.prop('checked'),
            "stageWritingSourceLangToTargetLang": checkboxStageWritingSrcToDst.prop('checked'),
            "stageSelfTestSourceLangToTargetLang": checkboxStageSelfTestSrcToDst.prop('checked'),
            "stageMosaicTargetLangToSourceLang": checkboxStageMosaicDstToSrc.prop('checked'),
            "stageOptionsTargetLangToSourceLang": checkboxStageOptionsDstToSrc.prop('checked'),
            "stageWritingTargetLangToSourceLang": checkboxStageWritingDstToSrc.prop('checked'),
            "stageSelfTestTargetLangToSourceLang": checkboxStageSelfTestDstToSrc.prop('checked')
        };
        updateSettings(_settings, function () {
            settings = _settings;
            bootstrap.Modal.getOrCreateInstance(document.getElementById('settings-modal')).hide();
        });
    });
}

function fillSettingsDialog() {
    const inputStageShowNumberOfWord = $('#input-stage-show-number-of-words');
    const inputStageOptionsNumberOfVariants = $('#input-stage-options-number-of-variants');
    const inputNumberOfWordsPerStage = $('#input-number-of-words-per-stage');
    const checkboxStageMosaicSrcToDst = $('#checkbox-stage-mosaic-src-to-dst');
    const checkboxStageOptionsSrcToDst = $('#checkbox-stage-options-src-to-dst');
    const checkboxStageWritingSrcToDst = $('#checkbox-stage-writing-src-to-dst');
    const checkboxStageSelfTestSrcToDst = $('#checkbox-stage-self-test-src-to-dst');
    const checkboxStageMosaicDstToSrc = $('#checkbox-stage-mosaic-dst-to-src');
    const checkboxStageOptionsDstToSrc = $('#checkbox-stage-options-dst-to-src');
    const checkboxStageWritingDstToSrc = $('#checkbox-stage-writing-dst-to-src');
    const checkboxStageSelfTestDstToSrc = $('#checkbox-stage-self-test-dst-to-src');
    inputStageOptionsNumberOfVariants.val(settings.stageOptionsNumberOfVariants);
    inputNumberOfWordsPerStage.val(settings.numberOfWordsPerStage);
    inputStageShowNumberOfWord.val(settings.stageShowNumberOfWords);
    checkboxStageMosaicSrcToDst.prop('checked', settings.stageMosaicSourceLangToTargetLang);
    checkboxStageOptionsSrcToDst.prop('checked', settings.stageOptionsSourceLangToTargetLang);
    checkboxStageWritingSrcToDst.prop('checked', settings.stageWritingSourceLangToTargetLang);
    checkboxStageSelfTestSrcToDst.prop('checked', settings.stageSelfTestSourceLangToTargetLang);
    checkboxStageMosaicDstToSrc.prop('checked', settings.stageMosaicTargetLangToSourceLang);
    checkboxStageOptionsDstToSrc.prop('checked', settings.stageOptionsTargetLangToSourceLang);
    checkboxStageWritingDstToSrc.prop('checked', settings.stageWritingTargetLangToSourceLang);
    checkboxStageSelfTestDstToSrc.prop('checked', settings.stageSelfTestTargetLangToSourceLang);
}

function onChangeDictionaryDialogMains(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');
    const numberOfRightAnswersInput = $('#' + dialogId + '-dictionary-dialog-number-of-right-answers');
    $('#' + dialogId + '-dictionary-dialog-save').prop('disabled', !(
            nameInput.val() &&
            sourceLangSelect.val() &&
            targetLangSelect.val() &&
            /^[0-9]+$/.test(numberOfRightAnswersInput.val().trim())
        )
    );
}

function createResourceDictionaryEntity(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');
    const numberOfRightAnswersInput = $('#' + dialogId + '-dictionary-dialog-number-of-right-answers');

    const dictionaryEntity = {};
    dictionaryEntity.name = nameInput.val().trim();
    dictionaryEntity.sourceLang = sourceLangSelect.val();
    dictionaryEntity.targetLang = targetLangSelect.val();
    dictionaryEntity.numberOfRightAnswers = numberOfRightAnswersInput.val();
    return dictionaryEntity;
}

function onSelectDictionary(dictionaries) {
    const selectedDictionaries = findSelectedDictionaries(dictionaries);
    if (selectedDictionaries.length !== 1) {
        return;
    }
    const selectedDictionary = selectedDictionaries[0];
    const deleteDictionaryPromptBody = $('#delete-dictionary-prompt-body');
    deleteDictionaryPromptBody.attr('item-id', selectedDictionary.dictionaryId);
    deleteDictionaryPromptBody.html(selectedDictionary.name);

    $('#edit-dictionary-dialog').attr('item-id', selectedDictionary.dictionaryId);
    $('#edit-dictionary-dialog-name').val(selectedDictionary.name)
    $('#edit-dictionary-dialog-source-lang').val(selectedDictionary.sourceLang);
    $('#edit-dictionary-dialog-target-lang').val(selectedDictionary.targetLang);
    $('#edit-dictionary-dialog-number-of-right-answers').val(selectedDictionary.numberOfRightAnswers);
}

function resetDictionarySelection() {
    disableDictionariesPageButtons();
    $('#dictionaries-btn-add').prop('disabled', false);
    $('#dictionaries-btn-settings').prop('disabled', false);
    $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
}

function drawRunPage(allDictionaries) {
    const selectedDictionaries = findSelectedDictionaries(allDictionaries);
    if (selectedDictionaries.length === 0) {
        return;
    }
    $('#dictionaries-btn-run').prop('disabled', true);
    toggleManageDictionariesPageButtons(true);
    const dictionaryMap = new Map(selectedDictionaries.map(function (dictionary) {
        return [dictionary.dictionaryId, dictionary];
    }));
    resetRowSelection($('#dictionaries tbody'));
    if (settings === undefined) {
        getSettings(function (res) {
            settings = res;
            getNextCardDeckAndDrawStageShow(dictionaryMap);
        });
    } else {
        getNextCardDeckAndDrawStageShow(dictionaryMap);
    }
}

function getNextCardDeckAndDrawStageShow(dictionaryMap) {
    getNextCardDeck(Array.from(dictionaryMap.keys()), settings.stageShowNumberOfWords, true, function (cards) {
        flashcards = cards;
        $.each(cards, function (index, card) {
            const dictionary = dictionaryMap.get(card.dictionaryId);
            card.dictionaryName = dictionary.name;
            card.numberOfRightAnswers = dictionary.numberOfRightAnswers;
        });
        stageShow();
    });
}

function findSelectedDictionaries(dictionaries) {
    const tbody = $('#dictionaries tbody');
    return findSelectedRows(tbody).map(function (index, row) {
        const rowId = $(row).attr('id');
        const id = rowId.slice(1);
        const res = dictionaries.find(it => it.dictionaryId === id);
        if (res === undefined) {
            return null;
        } else {
            return res;
        }
    }).filter(function (item) {
        return item != null;
    }).get();
}

function toggleManageDictionariesPageButtons(disabled) {
    const btnCards = $('#dictionaries-btn-cards');
    const btnDelete = $('#dictionaries-btn-delete');
    const btnEdit = $('#dictionaries-btn-edit');
    const btnDownload = $('#dictionaries-btn-download');
    btnCards.prop('disabled', disabled);
    btnEdit.prop('disabled', disabled);
    btnDelete.prop('disabled', disabled);
    btnDownload.prop('disabled', disabled);
}

function disableDictionariesPageButtons() {
    $('#dictionaries-btn-group button').each(function (i, b) {
        $(b).prop('disabled', true);
    });
}