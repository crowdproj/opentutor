/*!
 * A js-script library that contains common function, which are not related to app directly.
 */

function uuid() {
    let res = '';
    for (let i = 0; i < 32; i++) {
        res += Math.floor(Math.random() * 16).toString(16).toUpperCase();
    }
    return insertAt(insertAt(insertAt(insertAt(res,20, '-'), 16, '-'), 12, '-'), 8, '-')
}

function insertAt(string, position, char) {
    return string.substring(0, position) + char + string.substring(position);
}