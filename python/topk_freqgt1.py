f = open('topk_freq-5000.txt')
fo = open('topk_freqgt1-5000.txt', 'w')
for l in f:
    ls = l.split('\t')
    mf = int(ls[2])
    if mf > 0:
        fo.write(l)
        print  l
        
f.close()
fo.close()