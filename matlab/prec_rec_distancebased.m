dml = importdata('../distanceMeasure_label.txt');
dml(:, 1:2) = dml(:,1:2)./1000;
[~, ind] = sort(dml(:,3));
dml = dml(ind, :);


dm = dml(:,3);
df = dml(:,4);
dl = dml(:,5);



prec_rec( dm, dl, 'plotROC', 0, 'holdFigure', 1, 'style', 'k-' );
prec_rec( df, dl, 'plotROC', 0, 'holdFigure', 1, 'style', 'b-' );

title('');
box on;
grid on;
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 2);
label = findobj(gcf, 'type', 'label');
set(label, 'fontsize', 14);
set(gca, 'linewidth', 2, 'fontsize', 12);
legend({'baseline', 'Distance based measure', 'Frequency'});
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