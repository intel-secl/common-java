
function getValueAtPath(documentJson, expressionPath) {
    var $ = JSON.parse(documentJson);
    var result = eval(expressionPath);
    return JSON.stringify(result);
}

function putValueAtPath(documentJson, expressionPathForParent, key, value) {
    var $ = JSON.parse(documentJson);
    var parent = eval(expressionPathForParent);
    parent[key] = value;
    return JSON.stringify($);
}

function putJsonValueAtPath(documentJson, expressionPathForParent, key, jsonValue) {
    var $ = JSON.parse(documentJson);
    var parent = eval(expressionPathForParent);
    var value = JSON.parse(jsonValue);
    parent[key] = value;
    return JSON.stringify($);
}
