f = open('../locationEntropy-1000.txt')
c = 0
n = 0
for l in f:
    ls = l.split()
    n += 1
    if float(ls[1]) <= 0.000001:
        c += 1
        
print c, n
f.close()