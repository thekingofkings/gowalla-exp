a = importdata('../topk_freq-200.txt');

ind_friends = find(a(:,4)==1);
friends_meet = a(ind_friends,:);
ind_frequentFriends = find(friends_meet(:,3) > 0);
freq_fri_meet = friends_meet(ind_frequentFriends, 3);
cdfplot(freq_fri_meet);
title('Frquent friends meeting');


ind_nonfriends = find(a(:,4)==0);
nonfriends_meet = a(ind_nonfriends, :);
ind_freq_nonFri = find(nonfriends_meet(:,3) > 0);
freq_nonFri = nonfriends_meet(ind_freq_nonFri, 3);

figure();
cdfplot(freq_nonFri);
title('Frequent nonfriends meeting');


ind_freq_a = find(a(:,3) > 0);
freq_a = a(ind_freq_a, 3);
figure();
cdfplot(freq_a);
title('Frequent overall meeting');