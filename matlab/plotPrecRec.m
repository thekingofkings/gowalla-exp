labelFriendship;
c = importdata('../feature-vectors.txt');

prec_rec( c(1,:), friendLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'b-' );
title('Co-location diversity features');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('clocDiversity.eps', '-dpsc');
% system('epstopdf clocDiversity.eps');


prec_rec( c(2,:), friendLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'r-' );
title('Weighted Frequency features');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('weightFreq.eps', '-dpsc');
% system('epstopdf weightFreq.eps');



% prec_rec( c(3,:), friendLabel, 'plotROC', 0, 'holdFigure', 1 );
title('Mutual Information features');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('mutualInfo.eps', '-dpsc');
% system('epstopdf mutualInfo.eps');


prec_rec( c(4,:), friendLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'c-' );
title('Interesting Score (PAKDD) features');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('interestingness.eps', '-dpsc');
% system('epstopdf interestingness.eps');


prec_rec( c(5,:), friendLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:' );
title('Frequency features');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('freq.eps', '-dpsc');
% system('epstopdf freq.eps');


prec_rec( c(6,:), friendLabel, 'plotROC', 0, 'holdFigure', 1, 'style', 'r-.' );
title('Mutual information over co-location set');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('miocl.eps', '-dpsc');
% system('epstopdf miocl.eps');


prec_rec( c(7,:), friendLabel, 'plotROC', 0 , 'holdFigure', 1, 'style', 'g-');
title('Mutual information over co-location set v3');
% set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
% print('miocl3.eps', '-dpsc');
% system('epstopdf miocl3.eps');

title('');
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 2);
label = findobj(gcf, 'type', 'label');
set(label, 'fontsize', 14);
set(gca, 'linewidth', 2, 'fontsize', 12);
legend({'baseline', 'colocation diversity', 'weighted frequency', 'interestingness', ...
    'frequency', 'mutual information', 'mutual informaiton over colocation'});
set(gcf, 'PaperUnits', 'inches', 'PaperPosition', [0 0 8 6]);
print('prec-rec.eps', '-dpsc');
system('epstopdf prec-rec.eps');
