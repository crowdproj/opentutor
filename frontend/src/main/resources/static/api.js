/*!
 * A js-script library that contains client-api calls
 */

let keycloak

const getAllDictionariesURI = '/v1/api/dictionaries/get-all'
const createDictionaryURI = '/v1/api/dictionaries/create'
const deleteDictionaryURI = '/v1/api/dictionaries/delete'
const downloadDictionaryURI = '/v1/api/dictionaries/download'
const uploadDictionaryURI = '/v1/api/dictionaries/upload'
const getAllCardsURI = '/v1/api/cards/get-all'
const searchCardsURI = '/v1/api/cards/search'
const createCardURI = '/v1/api/cards/create'
const updateCardURI = '/v1/api/cards/update'
const learnCardURI = '/v1/api/cards/learn'
const resetCardURI = '/v1/api/cards/reset'
const deleteCardURI = '/v1/api/cards/delete'
const getAudioURI = '/v1/api/sounds/get'

const getAllDictionariesRequestType = 'getAllDictionaries'
const createDictionaryRequestType = 'createDictionary'
const deleteDictionaryRequestType = 'deleteDictionary'
const downloadDictionaryRequestType = 'downloadDictionary'
const uploadDictionaryRequestType = 'uploadDictionary'
const getAllCardsRequestType = 'getAllCards'
const searchCardsRequestType = 'searchCards'
const createCardRequestType = 'createCard'
const updateCardRequestType = 'updateCard'
const learnCardRequestType = 'learnCard'
const resetCardRequestType = 'resetCard'
const deleteCardRequestType = 'deleteCard'
const getAudioRequestType = 'getAudio'

async function initKeycloak() {
    if (devMode) {
        keycloak = null
        return
    }
    const res = new Keycloak({
        url: keycloakAuthURL,
        realm: keycloakAppRealm,
        clientId: keycloakAppClient,
    })
    await res.init({
        onLoad: 'check-sso',
    }).catch(function (error) {
        throw new Error('keycloak-init-error::' + error)
    })
    keycloak = res
}

function getDictionaries(onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': getAllDictionariesRequestType
    }
    post(getAllDictionariesURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone(res.dictionaries)
            }
        }
    })
}

function getCards(dictionaryId, onDone) {
    const data = {
        'dictionaryId': dictionaryId,
        'requestId': uuid(),
        'requestType': getAllCardsRequestType
    }
    post(getAllCardsURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone(res.cards)
            }
        }
    })
}

function getNextCardDeck(dictionaryIds, length, onDone) {
    if (length == null) {
        length = numberOfWordsToShow
    }
    const data = {
        'dictionaryIds': dictionaryIds,
        'requestId': uuid(),
        'requestType': searchCardsRequestType,
        'random': true,
        'length': length
    }
    post(searchCardsURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone(res.cards)
            }
        }
    })
}

function uploadDictionary(arrayBuffer, onDone, onFail) {
    const base64 = arrayBufferToBase64(arrayBuffer)
    const data = {
        'requestId': uuid(),
        'requestType': uploadDictionaryRequestType,
        'resource': base64
    }
    post(uploadDictionaryURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
            onFail()
        } else {
            if (onDone !== undefined) {
                onDone()
            }
        }
    })
}

function downloadDictionary(dictionaryId, downloadFilename, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': downloadDictionaryRequestType,
        'dictionaryId': dictionaryId
    }
    post(downloadDictionaryURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            const bytes = base64StringToUint8Array(res.resource).buffer
            const blob = new Blob([bytes], {type: "application/xml"})
            const link = document.createElement('a')
            link.href = window.URL.createObjectURL(blob)
            link.download = downloadFilename
            link.click()
            setTimeout(function () {
                window.URL.revokeObjectURL(link)
            }, 0)
            if (onDone !== undefined) {
                onDone()
            }
        }
    })
}

function createDictionary(dictionaryEntity, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': createDictionaryRequestType,
        'dictionary': dictionaryEntity
    }
    post(createDictionaryURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone(res.dictionary.dictionaryId)
            }
        }
    })
}

function deleteDictionary(dictionaryId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': deleteDictionaryRequestType,
        'dictionaryId': dictionaryId
    }
    post(deleteDictionaryURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone()
        }
    })
}

function createCard(card, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': createCardRequestType,
        'card': card
    }
    post(createCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone(res.card.cardId)
            }
        }
    })
}

function updateCard(card, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': updateCardRequestType,
        'card': card
    }
    post(updateCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.card.cardId)
        }
    })
}

function deleteCard(cardId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': deleteCardRequestType,
        'cardId': cardId
    }
    post(deleteCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone()
            }
        }
    })
}

function resetCard(cardId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': resetCardRequestType,
        'cardId': cardId
    }
    post(resetCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone()
            }
        }
    })
}

function learnCard(learns, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': learnCardRequestType,
        'cards': learns
    }
    post(learnCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            if (onDone !== undefined) {
                onDone()
            }
        }
    })
}

function playAudio(resourcePath, callback) {
    if (!callback) {
        callback = () => {
        }
    }
    const path = resourcePath.split(":")
    const data = {
        'requestId': uuid(),
        'requestType': getAudioRequestType,
        'lang': path[0],
        'word': path[1]
    }
    post(getAudioURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            const bytes = base64StringToUint8Array(res.resource).buffer
            const blob = new Blob([bytes], {type: 'audio/wav'})
            const url = window.URL.createObjectURL(blob)
            new Audio(url).play().then(callback)
        }
    })
}

function hasResponseErrors(res) {
    return res.errors !== undefined && res.errors.length !== 0
}

function handleResponseErrors(res) {
    if (devMode) {
        console.log('errors: ' + res.errors.map(it => it.message))
    }
}

function post(url, requestData, onDone, onFail) {
    if (onFail === undefined) {
        onFail = function (error) {
            if (devMode) {
                console.log('post-error: ' + error)
            }
        }
    }
    if (devMode) {
        $.ajax({
            type: 'POST',
            url: url,
            contentType: 'application/json',
            data: JSON.stringify(requestData),
        }).done(onDone).fail(onFail)
    } else {
        authPost(url, requestData, onDone, onFail)
    }
}

function authPost(url, requestData, onDone, onFail, runAgain) {
    if (runAgain === undefined) {
        runAgain = true
    }
    // noinspection JSUnusedLocalSymbols
    $.ajax({
        type: 'POST',
        url: url,
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        headers: {'Authorization': 'Bearer ' + keycloak.token}
    }).done(onDone).fail(function (jqXHR, status, error) {
        const code = jqXHR.status
        if (code === 401 && runAgain) {
            // try again
            keycloak.updateToken().then(function () {
                authPost(url, requestData, onDone, onFail, false)
            })
        } else {
            onFail(error)
        }
    })
}

function logout() {
    if (devMode) {
        return
    }
    keycloak.logout().catch(function (error) {
        throw new Error('logout-error::' + error)
    })
}