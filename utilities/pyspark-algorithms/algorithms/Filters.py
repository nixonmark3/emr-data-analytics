
# convert a dictionary of features to an array of doubles (in order)
def dictionaryToArray(dict, keys):
    result = []
    for key in keys.split(","):
        result.append(dict[key])
    return result

