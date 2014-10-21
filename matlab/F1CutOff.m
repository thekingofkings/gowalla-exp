f = figure();
hold on;
grid on;
box on;
set(gca, 'xscale', 'log');


load('prec-rec-cutof-gw.mat');
F1 = prec .* recl;
plot(cutoff, F1, 'b-');

[~, idx] = max(F1);
cutoff(idx)

load('prec-rec-cutof-bk.mat');
F1 = prec .* recl;
plot(cutoff, F1, 'r--');

[~, idx] = max(F1);
cutoff(idx)

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('PGT measure', 'fontsize', 20);
ylabel('F1', 'fontsize', 20);


set(gca, 'linewidth', 3, 'fontsize', 20, 'xtick', [0.01, 0.1, 1, 10, 100, 1000]);
legend({'Gowalla', 'Brightkite'}, 'location', 'northeast');


set(gcf, 'PaperUnits', 'inches');
print('F1Cutoff.eps', '-dpsc');
system('epstopdf F1Cutoff.eps');