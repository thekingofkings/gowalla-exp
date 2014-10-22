f1 = figure();
hold on;
grid on;
box on;
set(gca, 'xscale', 'log');
axis([0.01, 500, 0, 0.4]);
set(gca, 'linewidth', 3, 'fontsize', 20, 'xtick', [0.01, 0.1, 1, 10, 100]);

f2 = figure();
hold on;
grid on;
box on;
set(gca, 'xscale', 'log');
axis([0.01, 500, 0, 1]);
set(gca, 'linewidth', 3, 'fontsize', 20, 'xtick', [0.01, 0.1, 1, 10, 100]);


load('prec-rec-cutof-gw.mat');
F1 = prec .* recl;

figure(f1);
plot(cutof, F1, 'linestyle', '-', 'color', [0, 0, 0.8]);

figure(f2);
plot(cutof, accu, 'linestyle', '-', 'color', [0, 0, 0.8]);
plot(cutof, base, 'linestyle', '--', 'color', [0.6, 0.5, 1]);



load('prec-rec-cutof-bk.mat');
F1 = prec .* recl;

figure(f1);
plot(cutof, F1, 'linestyle', '-.', 'color', [0.8 ,0,0]);

figure(f2);
plot(cutof, accu, 'linestyle', '-.', 'color', [0.8 ,0,0]);
plot(cutof, base, 'linestyle', '--', 'color', 'red');



figure(f1);
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('PGT measure', 'fontsize', 20);
ylabel('F1', 'fontsize', 20);

legend({'Gowalla', 'Brightkite'}, 'location', 'southwest');

set(gcf, 'PaperUnits', 'inches');
print('F1Cutoff.eps', '-dpsc');
system('epstopdf F1Cutoff.eps');


figure(f2);
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('PGT measure', 'fontsize', 20);
ylabel('Accuracy', 'fontsize', 20);
legend({'Gowalla', 'Gowalla baseline', 'Brightkite', 'Brightkite baseline'}, 'location', 'southeast');

set(gcf, 'PaperUnits', 'inches');
print('AccuCutoff.eps', '-dpsc');
system('epstopdf AccuCutoff.eps');