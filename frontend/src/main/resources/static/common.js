/*!
 * A js-script library that contains common function, which are not related to app directly.
 */

function uuid() {
    let res = '';
    for (let i = 0; i < 32; i++) {
        res += Math.floor(Math.random() * 16).toString(16).toUpperCase();
    }
    return insertAt(insertAt(insertAt(insertAt(res, 20, '-'), 16, '-'), 12, '-'), 8, '-')
}

function insertAt(string, position, char) {
    return string.substring(0, position) + char + string.substring(position);
}

/**
 * Creates a random array from the given one.
 * @param data an array
 * @param length a length of new array
 * @returns {*[]} a new array
 */
function randomArray(data, length) {
    if (length > data.length) {
        throw "Wrong input: " + length + " > " + data.length;
    }
    const res = data.slice();
    shuffleArray(res);
    if (length === data.length) {
        return res;
    }
    return res.slice(0, length);
}

/**
 * Randomly permutes the specified array.
 * All permutations occur with approximately equal likelihood.
 * @param array
 */
function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
}

/**
 * Removes duplicates.
 * @param array
 * @returns {*[]} - a new array
 */
function removeDuplicates(array) {
    const res = [];
    $.each(array, function (i, e) {
        if ($.inArray(e, res) === -1) res.push(e);
    });
    return res;
}

/**
 * Splits string using separator.
 * @param value {string}
 * @param separator {string}
 * @returns {*[]|*|string[]}
 */
function toArray(value, separator) {
    value = value.trim();
    if (value === '') {
        return [];
    }
    return value.split(separator).map(x => x.trim());
}

/**
 * Prepares the filename to save.
 * @param string
 * @returns {string}
 */
function toFilename(string) {
    return string.replace(/[^a-z\d]/gi, '_').toLowerCase();
}

function base64StringToUint8Array(base64) {
    const str = window.atob(base64);
    const length = str.length;
    const uint8Array = new Uint8Array(length);
    for (let i = 0; i < length; i++) {
        uint8Array[i] = str.charCodeAt(i);
    }
    return uint8Array
}

function arrayBufferToBase64(buffer) {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}
