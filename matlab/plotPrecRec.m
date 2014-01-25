labelFriendship;
c = importdata('../feature-vectors.txt');

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


prec_rec( c(4,:), friendLabel );
title('Interesting Score (PAKDD) features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('interestingness.eps', '-dpsc');
system('epstopdf interestingness.eps');


prec_rec( c(5,:), friendLabel );
title('Frequency features');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('freq.eps', '-dpsc');
system('epstopdf freq.eps');


prec_rec( c(6,:), friendLabel );
title('Mutual information over co-location set');
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 3]);
print('miocl.eps', '-dpsc');
system('epstopdf miocl.eps');


