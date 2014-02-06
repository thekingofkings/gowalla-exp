clc;
dml5 = importdata('../distanceMeasure_label.txt');
[~, ind] = sort(dml5(:,5));
dml5 = dml5(ind, :)
dm5 = dml5(:,3);
df5 = dml5(:,4);
locm5 = dml5(:,5);
locf5 = dml5(:,6);
dl5 = dml5(:,7);

prec_rec( dm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
prec_rec( df5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c-' );
prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b--' );
prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'm--' );


title('');
box on;
grid on;
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('Recall', 'fontsize', 14);
ylabel('Precision', 'fontsize', 14);
set(gca, 'linewidth', 2, 'fontsize', 12);
legend({'baseline', 'Distance based (30m) measure', 'Distance based (30m) frequency', ...
    'Location ID measure', 'Location ID frequency'});
set(gcf, 'PaperUnits', 'inches');
print('prec-rec-loc-measure-50.eps', '-dpsc');
system('epstopdf prec-rec-loc-measure-50.eps');