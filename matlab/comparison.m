condition = 1;

alpha = 0.2;
beta = 0.8;
% load sigmod results
% ua, ub, co-location entropy, weighted frequency, frequency, friend label
sigmod = importdata('../sigmod13.txt');
sigmod = sigmod(sigmod(:,5) > condition,:); % meeting frequency > condition
sigmod_score = alpha * sigmod(:,3) + beta * sigmod(:,4);
sigmod_label = sigmod(:,6);


% load our results
% ua, ub, combination, weighted frequency, personal, 
% frequency, frind label
we = importdata('../distance-d30-u5000c1.50000-101s.txt');
we = we(we(:,6) > condition, :);    % meeting frequency > condition
we_score = we(:,3);
we_label = we(:,7);

f = figure();
hold on;
box on;
grid on;
precisionRecallPlot(sigmod_score, sigmod_label, 'b-');
precisionRecallPlot(we_score, we_label, 'r--');
% precisionRecallPlot(we(:,4), we_label, 'y--');
% precisionRecallPlot(sigmod(:,4), sigmod_label, 'y-');
% precisionRecallPlot(we(:,6), we_label, 'g--');
% precisionRecallPlot(sigmod(:,5), sigmod_label, 'g-');
a = findobj(gcf, 'type', 'line');
set(a, 'linewidth', 3);
set(gca, 'linewidth', 2, 'fontsize', 20);
xlabel('Recall', 'fontsize', 20);
ylabel('Precision', 'fontsize', 20);
legend({'P+G+T', 'EBM'}, 'location', 'southwest');
print('compare.eps', '-dpsc');
system('epstopdf compare.eps');




