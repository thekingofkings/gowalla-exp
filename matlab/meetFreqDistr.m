a = importdata('../topk_freq-200.txt');
% eliminate the 0 meeting frequency pair
freq_a = a(a(:,3)>0,:);

ind_friends = find(freq_a(:,4)==1);
friends_meet = freq_a(ind_friends,:);
% ind_frequentFriends = find(friends_meet(:,3) > 0);
freq_fri_meet = friends_meet(ind_frequentFriends, 3);
l1 = cdfplot(freq_fri_meet);
hold on;
set(l1, 'linewidth', 2, 'color', 'green');


ind_nonfriends = find(freq_a(:,4)==0);
nonfriends_meet = freq_a(ind_nonfriends, :);
% ind_freq_nonFri = find(nonfriends_meet(:,3) > 0);
freq_nonFri = nonfriends_meet(ind_freq_nonFri, 3);
l2 = cdfplot(freq_nonFri);
set(l2, 'linewidth', 2, 'color', 'blue');



% ind_freq_a = find(a(:,3) > 0);
% freq_a = a(ind_freq_a, 3);
l3 = cdfplot(freq_a(:,3));
set(l3, 'linewidth', 2, 'color', 'red', 'linestyle', '-.');

set(gca, 'xscale', 'log', 'fontsize', 14);
legend({'Friends', 'Non-friends', 'Overall'}, 'fontsize', 14, ...
    'location', 'best');
title('');
ylabel('CDF of meeting frequency', 'fontsize', 16);
xlabel('Meeting frequency', 'fontsize', 16);
saveas(gcf, 'cdfMeetFreq.jpg');
print('cdfMeetFreq.eps', '-dpsc');
system('epstopdf cdfMeetFreq.eps');