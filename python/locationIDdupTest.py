import re

f = open('Gowalla_totalCheckins.txt')
latis = dict()
longis = dict()
cnt = dict()
for l in f:
    ls = re.split('\s+', l)
    id = int(ls[4])
    lati = float(ls[2])
    lon = float(ls[3])
    
    
    if id in latis:
        cnt[id] += 1
        if lati in latis[id] and lon in longis[id]:
            pass
        else:
            latis[id].append(lati)
            longis[id].append(lon)
    else:
        latis[id] = [lati]
        longis[id] = [lon]
        cnt[id] = 0
        
f.close()        
        
        
c = 0        
fo = open('test.txt', 'w')
for k, v in latis.items():
    if len(v)  == 2 :
        c += 1
        print k, cnt[k], v, longis[k]
        
        
print c, len(latis)        