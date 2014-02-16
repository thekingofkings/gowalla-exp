import re

f = open('Gowalla_totalCheckins.txt')
gps = dict()
cnt = dict()
for l in f:
    ls = re.split('\s+', l)
    id = int(ls[4])
    lati = ls[2]
    lon = ls[3]
    
    key = lati + lon
    if key in gps:
        cnt[key] += 1
        if id in gps[key]:
            pass
        else:
            gps[key].append( id )
    else:
        gps[key] = [id]
        cnt[key] = 0
        
f.close()        
        
        
c = 0        
fo = open('test.txt', 'w')
for k, v in gps.items():
    if len(v)  != 1 :
        c += 1
        fo.write( ' '.join([k, str(c), str(v), '\n']) )
        print k, c, v
fo.close()        
        
        
print c, len(gps)    