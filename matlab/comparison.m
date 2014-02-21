condition = 2;

alpha = 0.1;
beta = 0.9;
% load sigmod results
% ua, ub, co-location entropy, weighted frequency, frequency, friend label
sigmod = importdata('../sigmod13.txt');
sigmod = sigmod(sigmod(:,5) > condition,:); % meeting frequency > condition
% normorlize
min_entro = min(sigmod(:,3));
delta_entro = max(sigmod(:,3)) - min(sigmod(:,3));
sigmod(:,3) = sigmod(:,3) - min_entro * ones(size(sigmod, 1), 1);
sigmod(:,3) = sigmod(:,3) / delta_entro;

min_w = min(sigmod(:,4));
delta_w = max(sigmod(:,4)) - min(sigmod(:,4));
sigmod(:,4) = sigmod(:,4) - min_w * ones(size(sigmod, 1), 1);
sigmod(:,4) = sigmod(:,4) / delta_w;

sigmod_score = alpha * sigmod(:,3) + beta * sigmod(:,4);
sigmod_label = sigmod(:,6);


% load our results
% ua, ub, combination, weighted frequency, personal, 
% frequency, frind label
we = importdata('../data_tunningTC/distance-d30-u5000c0.200000-101s.txt');
we = we(we(:,6) > condition, :);    % meeting frequency > condition
we_score = we(:,3);
we_label = we(:,7);

f = figure();
hold on;
box on;
grid on;
set(0, 'defaultlinelinewidth', 3');
precisionRecallPlot(sigmod(:,5), sigmod_label, 'linestyle', '-.', 'color', [0,0.7,0] );

% precisionRecallPlot(we(:,4), we_label, 'y--');
precisionRecallPlot(sigmod(:,4), sigmod_label, 'm:', 'linewidth', 5);   % weighted freq
precisionRecallPlot(sigmod(:,3), sigmod_label, 'k-');   % co-location entropy
% precisionRecallPlot(we(:,6), we_label, 'g--');
precisionRecallPlot(sigmod_score, sigmod_label, 'linestyle', '--', 'color', [0.7 ,0,0] );
precisionRecallPlot(we_score, we_label, 'linestyle', '-', 'color', [0,0,0.7] );

set(gca, 'linewidth', 2, 'fontsize', 20);
xlabel('Recall', 'fontsize', 20);
ylabel('Precision', 'fontsize', 20);
legend({'Meeting Frequency', 'Weighted Frequency', 'Location Diversity', 'EBM', 'Our Method'}, 'location', 'southwest');
print('compare.eps', '-dpsc');
system('epstopdf compare.eps');




