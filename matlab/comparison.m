condition = 0;
tau = 1;

if tau == 1
% use 1 hour results
    sigmod = importdata('../data/sigmod13-u5000-1hour.txt');
    we = importdata('../data_tunningTC/tuneTC-u5000-t1.000-c0.200.txt');
elseif tau == 4
% use 4 hour results
    sigmod = importdata('../data/sigmod13-u5000.txt');
    we = importdata('../data_tunningTC/distance-d30-u5000-tc0.20.txt');
end


alpha = 0.6;
beta = 0.4;
% load sigmod results
% ua, ub, co-location entropy, weighted frequency, frequency, friend label

sigmod = sigmod(sigmod(:,5) > condition,:); % meeting frequency > condition
sigmod_diversity = sigmod(:,3);
sigmod_weightFreq = sigmod(:,4);
sigmod_freq = sigmod(:,5);


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
we = we(we(:,6) > condition, :);    % meeting frequency > condition
we_score = we(:,7);
we_label = we(:,9);

f = figure();
hold on;
box on;
grid on;
set(0, 'defaultlinelinewidth', 3');
precisionRecallPlot(sigmod_freq, sigmod_label, 'linestyle', '-', 'color',  [0, 0, 0.8]);

% precisionRecallPlot(we(:,4), we_label, 'y--');
precisionRecallPlot(sigmod_weightFreq, sigmod_label, 'color', [0, 0.5, 0], ...
    'linestyle', '-.');   % weighted freq
precisionRecallPlot(sigmod_diversity, sigmod_label, 'color', [210,105,30]/255, ...
    'linestyle', ':', 'linewidth', 5);  % co-location entropy
% precisionRecallPlot(we(:,6), we_label, 'g--');
precisionRecallPlot(sigmod_score, sigmod_label, 'linestyle', '--', 'color', [0.7 ,0,0] );
[~, ~, ~, prec, recl, cutof, accu] = precisionRecallPlot(we_score, we_label, 'linestyle', '-', 'color', [0.5, 0.4, 0.9]);


%======== check the consistency with previous figures ================
% precisionRecallPlot(we(:,4), we_label, 'y--');
% precisionRecallPlot(we(:,6), we_label, 'g--');


set(gca, 'linewidth', 2, 'fontsize', 20);
xlabel('Recall', 'fontsize', 20);
ylabel('Precision', 'fontsize', 20);
legend({'Meeting Frequency', 'Weighted Frequency', 'Location Diversity', ...
    'EBM', 'PGT'}, 'location', 'southwest');
print('compare.eps', '-dpsc');
system('epstopdf compare.eps');
% saveas(gcf, 'compare.png');


F1 = prec .* recl;
base = ones(1,100) * (1 -  sum(we_label) / length(we_label));
f2 = figure();
hold on;
box on;

plot(cutof, F1, 'b-');
plot(cutof, accu, 'r-.');
plot(cutof, base, 'g--');
set(gca, 'xscale', 'log');
save('prec-rec-cutof-gw.mat', 'prec', 'cutof', 'recl', 'accu', 'base');