dml = importdata('../distanceMeasure_label-100.txt');
dml(:, 1:2) = dml(:,1:2)./1000;
[~, ind] = sort(dml(:,3));
dml = dml(ind, :);


dm = dml(:,3);
df = dml(:,4);
locidf = dml(:,5);
dl = dml(:,6);



prec_rec( dm, dl, 'plotROC', 0, 'holdFigure', 1, 'style', 'k--' );
prec_rec( df, dl, 'plotROC', 0, 'holdFigure', 1, 'style', 'b-' );
prec_rec( locidf, dl, 'plotROC', 0, 'holdFigure', 1, 'style', 'r-' );


dml5 = importdata('../distanceMeasure_label-50.txt');
dm5 = dml5(:,3);
df5 = dml5(:,4);
dl5 = dml5(:,6);

prec_rec( dm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
prec_rec( df5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c-' );


dml3 = importdata('../distanceMeasure_label-30.txt');
dm3 = dml3(:,3);
df3 = dml3(:,4);
dl3 = dml3(:,6);

prec_rec( dm3, dl3, 'plotROC', 0, 'holdFigure', 1, 'style', 'y-.' );
prec_rec( df3, dl3, 'plotROC', 0, 'holdFigure', 1, 'style', 'm-.' );


title('');
box on;
grid on;
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('Recall', 'fontsize', 14);
ylabel('Precision', 'fontsize', 14);
set(gca, 'linewidth', 2, 'fontsize', 12);
legend({'baseline', 'Distance based (100m) measure', 'Distance based (100m) frequency', ...
    'Location ID frequency', 'Distance based (50m) measure', ...
    'Distance based (50m) frequency', ...
    'Distance based (30m) measure', 'Distance based (30m) frequency'});
set(gcf, 'PaperUnits', 'inches');
print('prec-rec-dist.eps', '-dpsc');
system('epstopdf prec-rec-dist.eps');



% a = 0.01:0.01:0.99;
% b = a';
% 
% res_base2 = log2(b) * log2(a);
% surf(res_base2);
% res_base10 = log2(b) * log2(a);
% hold on;
% surf(res_base10);