labelFriendship;
c = importdata('feature-vectors.txt');

prec_rec( c(1,:), friendLabel );
title('Co-location diversity features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('clocDiversity.eps', '-dpsc');
system('epstopdf clocDiversity.eps');


prec_rec( c(2,:), friendLabel );
title('Weighted Frequency features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('weightFreq.eps', '-dpsc');
system('epstopdf weightFreq.eps');



prec_rec( c(3,:), friendLabel );
title('Mutual Information features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('mutualInfo.eps', '-dpsc');
system('epstopdf mutualInfo.eps');


d = importdata('feature-vectors-interestingness.txt');
prec_rec( d, friendLabel );
title('Interesting Score (PAKDD) features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('interestingness.eps', '-dpsc');
system('epstopdf interestingness.eps');


e = importdata('feature-vectors-freq.txt');
prec_rec( e(2,:), friendLabel );
title('Frequency features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('freq.eps', '-dpsc');
system('epstopdf freq.eps');


