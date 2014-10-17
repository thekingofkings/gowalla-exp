f = figure();
hold on;
grid on;
box on;
set(gca, 'xscale', 'log');


load('prec-rec-cutof-bk.mat');
F1 = prec .* recl;
plot(cutoff, F1, 'r-');


[~, idx] = max(F1);
cutoff(idx), mean(freq)


load('prec-rec-cutof-gw.mat');
F1 = prec .* recl;
plot(cutoff, F1, 'b-');


[~, idx] = max(F1);
cutoff(idx), mean(freq)



hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
xlabel('Recall', 'fontsize', 20);
ylabel('Precision', 'fontsize', 20);


set(gca, 'linewidth', 3, 'fontsize', 20);