/*!
 * page:dictionaries js-script library.
 */

function drawDictionariesPage() {
    getDictionaries(function (dictionaries) {
        displayPage('dictionaries');

        const tbody = $('#dictionaries tbody');

        tbody.html('');
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

        bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-dictionary-prompt')).hide();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('add-dictionary-dialog')).hide();
        initDictionaryDeletePrompt();
        initDictionaryDialog('add');

        $.each(dictionaries, function (index, dictionary) {
            let row = $(`<tr id="${'d' + dictionary.dictionaryId}">
                            <td>${dictionary.sourceLang}</td>
                            <td>${dictionary.targetLang}</td>
                            <td>${dictionary.name}</td>
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

function initDictionaryDialog(dialogId) {
    $('#dictionary-btn-' + dialogId).off('click').on('click', function () { // push open dialog
        onChangeDictionaryDialogMains(dialogId);
    });

    $('#' + dialogId + '-dictionary-dialog-name').off('input').on('input', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#' + dialogId + '-dictionary-dialog-source-lang').off('change').on('change', function () {
        onChangeDictionaryDialogMains(dialogId);
    });
    $('#' + dialogId + '-dictionary-dialog-target-lang').off('change').on('change', function () {
        onChangeDictionaryDialogMains(dialogId);
    });

    $('#' + dialogId + '-dictionary-dialog-save').off('click').on('click', function () {
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

function onChangeDictionaryDialogMains(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');
    $('#' + dialogId + '-dictionary-dialog-save')
        .prop('disabled', !(nameInput.val() && sourceLangSelect.val() && targetLangSelect.val()));
}

function createResourceDictionaryEntity(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');

    const dictionaryEntity = {};
    dictionaryEntity.name = nameInput.val().trim();
    dictionaryEntity.sourceLang = sourceLangSelect.val();
    dictionaryEntity.targetLang = targetLangSelect.val();
    return dictionaryEntity;
}

function onSelectDictionary(dictionaries) {
    const selectedDictionaries = findSelectedDictionaries(dictionaries);
    if (selectedDictionaries.length !== 1) {
        return;
    }
    const selectedDictionary = selectedDictionaries[0];
    const body = $('#delete-dictionary-prompt-body');
    body.attr('item-id', selectedDictionary.dictionaryId);
    body.html(selectedDictionary.name);
}

function resetDictionarySelection() {
    disableDictionariesPageButtons();
    $('#dictionaries-btn-add').prop('disabled', false)
    $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
}

function drawRunPage(allDictionaries) {
    const selectedDictionaries = findSelectedDictionaries(allDictionaries)
    if (selectedDictionaries.length === 0) {
        return;
    }
    $('#dictionaries-btn-run').prop('disabled', true);
    toggleManageDictionariesPageButtons(true);
    const dictionaryMap = new Map(selectedDictionaries.map(function (dictionary) {
        return [dictionary.dictionaryId, dictionary.name];
    }));
    resetRowSelection($('#dictionaries tbody'));
    getNextCardDeck(Array.from(dictionaryMap.keys()), numberOfWordsToShow, true, function (cards) {
        flashcards = cards;
        $.each(cards, function (index, card) {
            card.dictionaryName = dictionaryMap.get(card.dictionaryId);
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
    const btnEdit = $('#dictionaries-btn-cards');
    const btnDelete = $('#dictionaries-btn-delete');
    const btnDownload = $('#dictionaries-btn-download');
    btnEdit.prop('disabled', disabled);
    btnDelete.prop('disabled', disabled);
    btnDownload.prop('disabled', disabled);
}

function disableDictionariesPageButtons() {
    $('#dictionaries-btn-group button').each(function (i, b) {
        $(b).prop('disabled', true);
    });
}