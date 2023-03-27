/*!
 * page:dictionaries js-script library.
 */

function drawDictionariesPage() {
    getDictionaries(function (response) {
        displayPage('dictionaries');

        const tbody = $('#dictionaries tbody');

        tbody.html('');
        initTableListeners('dictionaries', resetDictionarySelection);

        $('#dictionaries-table-row').css('height', calcInitTableHeight());

        $('#dictionaries-btn-run').off().on('click', drawRunPage);
        $('#dictionaries-btn-cards').off().on('click', drawDictionaryPage);
        $('#dictionaries-btn-upload').off().on('click', () => {
            $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
        }).on('change', (e) => {
            const file = e.target.files[0];
            if (file !== undefined) {
                uploadDictionaryFile(file);
            }
        });
        $('#dictionaries-btn-download').off().on('click', downloadDictionaryFile);

        bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-dictionary-prompt')).hide();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('add-dictionary-dialog')).hide();
        initDictionaryDeletePrompt();
        initDictionaryDialog('add');

        $.each(response, function (key, value) {
            let row = $(`<tr id="${'d' + value.dictionaryId}">
                            <td>${value.sourceLang}</td>
                            <td>${value.targetLang}</td>
                            <td>${value.name}</td>
                            <td>${value.total}</td>
                            <td>${value.learned}</td>
                          </tr>`);
            row.on('click', function () {
                dictionaryRowOnClick(row, value);
            });
            row.dblclick(drawRunPage);
            tbody.append(row);
        });
    });
}

function uploadDictionaryFile(file) {
    const btnUpload = $('#dictionaries-btn-upload-label');
    const reader = new FileReader()
    reader.onload = function (e) {
        const txt = e.target.result;
        uploadDictionary(txt, drawDictionariesPage, function () {
            btnUpload.addClass('btn-outline-danger');
        });
    }
    reader.readAsArrayBuffer(file);
    $('#dictionaries-btn-upload').val('');
}

function downloadDictionaryFile() {
    if (selectedDictionary == null) {
        return;
    }
    const filename = toFilename(selectedDictionary.name) + '-' + new Date().toISOString().substring(0, 19) + '.xml';
    downloadDictionary(selectedDictionary.dictionaryId, filename);
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
    initLanguageSelector('#' + dialogId + '-dictionary-dialog-source-lang', ['en', 'ru']);
    initLanguageSelector('#' + dialogId + '-dictionary-dialog-target-lang', ['en', 'ru']);
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

function initLanguageSelector(id, tags) {
    const select = $(id).html('').append($(`<option value="-1"></option>`));
    $.each(tags, function (index, value) {
        select.append($(`<option value="${index}">${value}</option>`));
    });
}

function onChangeDictionaryDialogMains(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');
    $('#' + dialogId + '-dictionary-dialog-save')
        .prop('disabled', !(nameInput.val() && sourceLangSelect.text() && targetLangSelect.text()));
}

function createResourceDictionaryEntity(dialogId) {
    const nameInput = $('#' + dialogId + '-dictionary-dialog-name');
    const sourceLangSelect = $('#' + dialogId + '-dictionary-dialog-source-lang option:selected');
    const targetLangSelect = $('#' + dialogId + '-dictionary-dialog-target-lang option:selected');

    const dictionaryEntity = {};
    dictionaryEntity.name = nameInput.val().trim();
    dictionaryEntity.sourceLang = sourceLangSelect.text();
    dictionaryEntity.targetLang = targetLangSelect.text();
    return dictionaryEntity;
}

function dictionaryRowOnClick(row, dict) {
    selectedDictionary = dict;
    const tbody = $('#dictionaries tbody');
    const btnRun = $('#dictionaries-btn-run');
    const btnEdit = $('#dictionaries-btn-cards');
    const btnDelete = $('#dictionaries-btn-delete');
    const btnDownload = $('#dictionaries-btn-download');
    resetRowSelection(tbody);
    markRowSelected(row);
    btnRun.prop('disabled', false);
    btnEdit.prop('disabled', false);
    btnDelete.prop('disabled', false);
    btnDownload.prop('disabled', false);

    const body = $('#delete-dictionary-prompt-body');
    body.attr('item-id', dict.dictionaryId);
    body.html(dict.name);
}

function resetDictionarySelection() {
    selectedDictionary = null;
    $('#dictionaries-btn-group button').each(function (i, b) {
        $(b).prop('disabled', true);
    });
    $('#dictionaries-btn-add').prop('disabled', false)
    $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
}

function drawRunPage() {
    if (selectedDictionary == null) {
        return;
    }
    resetRowSelection($('#dictionaries tbody'));
    getNextCardDeck(selectedDictionary.dictionaryId, null, function (array) {
        flashcards = array;
        stageShow();
    });
}