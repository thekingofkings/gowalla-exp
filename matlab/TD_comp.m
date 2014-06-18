dml6 = importdata('../data_sensitivity/distance-d30-u5000-10.txt');

dml6 = dml6(dml6(:,6)>1,:);

fri = dml6(dml6(:,9)==1,:);
nonfri = dml6(dml6(:,9)==0,:);

td_fri = fri(:,8);
td_nonfri = nonfri(:,8);


figure;
hold on;

l1 = cdfplot(td_fri);
set(l1, 'color', 'red', 'linewidth', 3);

l2 = cdfplot(td_nonfri);
set(l2, 'color', 'blue', 'linewidth', 3, 'linestyle', '--');

set(gca, 'xscale', 'log');
legend('Friend', 'Non-friend');