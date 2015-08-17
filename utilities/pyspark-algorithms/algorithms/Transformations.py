
def deNormalize(data, mu, sigma):
    return [(x+y) for x,y in zip([(a*b) for a,b in zip(data, sigma)], mu)]

def dotProduct(v1, v2):
    return [sum([a*b for a,b in zip(v1, v2)])]

def normalize(data, mu, sigma):
    return [(x/y) for x,y in zip([(a-b) for a,b in zip(data, mu)], sigma)]