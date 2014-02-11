% plot the cdf of user count and location count
a = importdata('../../../dataset/userCount.txt');

cdfplot(a(:,1));
title('Records number per user CDF');


b = importdata('../../../dataset/locCount.txt');

figure();
cdfplot(b(:,1));
title('Visiting number per location CDF');