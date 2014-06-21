"""In the current fold, you will find the following file:
        distance-d30-u51406-c0.200.txt
    which records all the pairs from Brightkite data, together with its weight.
    
    The purpose of this Python script is to sample from this complete meeting pair file,
    to get a subset of meeting pairs with top $numUser$ users.
"""

import re
from itertools import islice


numUser = 3000
users = set()

# user list (ranked by number of check-ins)
f1 = open('userCount.txt', 'r')

for line in islice(f1, numUser):
    ls = re.split("\s+", line)
    uid = int(ls[0])
    
    users.add(uid)
    
f1.close()


# all pairs meeting events
f2 = open('distance-d30-u107092-c0.200.txt')

f3 = open('distance-d30-u{0}-c0.200.txt'.format(numUser), 'w')

for line in f2:
    ls = re.split("\s+", line)
    u1 = int(ls[0])
    u2 = int(ls[1])
    if u1 in users and u2 in users:
        f3.write(line)
        
f2.close()
f3.close()