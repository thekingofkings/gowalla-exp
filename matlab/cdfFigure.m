a = importdata('userCount.txt');

cdfplot(a(:,1));
title('Records number per user CDF');


b = importdata('locCount.txt');

figure();
cdfplot(b(:,1));
title('Visiting number per location CDF');