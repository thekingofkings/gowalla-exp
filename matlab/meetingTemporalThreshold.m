flist = ls('../data_meeting_temp/meTau-u5000-d0*');
fn = size(flist,1);
figure();
hold on;

para_delta = zeros(fn, 1);
precisions = zeros(fn, 3);
avg_freq = zeros(fn,1);

for c = 1:fn
    para_delta(c) = sscanf(flist(c,:), 'meTau-u5000-d0.03-t%f');
    dml5 = importdata(['../data_meeting_temp/', flist(c,:)]);
    dml5 = dml5(dml5(:,6) > 0, :);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);

    locm5 = dml5(:,6);
    freq = dml5(:,6);
    avg_freq(c) = mean(freq);
    
    dl5 = dml5(:,9);

    precisions(c,:) =  precisionRecallPlot( locm5, dl5 );
end

    [~, ind] = sort(para_delta);
    para_delta = para_delta(ind);
    precisions = precisions(ind, :);
    avg_freq = avg_freq(ind);
    
    plot(para_delta, precisions(:,1), 'd--', 'color', [0.7, 0.3, 0.3]);
    plot(para_delta, precisions(:,2), 'o--', 'color', [0.3, 0.3, 0.7]);
    plot(para_delta, precisions(:,3), 'v--', 'color', [0.3, 0.7, 0.3]);
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3, 'markersize', 14);
    xlabel('Meeting temporal threshold $\tau$ (hour)', 'fontsize', 20, 'interpreter' ,'latex');
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18, 'linewidth', 2);
    legend({'Recall 0.3', 'Recall 0.5', 'Recall 0.7'}, 'location', 'best');
    set(gcf, 'PaperUnits', 'inches');
    print('meeting-tempThreshold.eps', '-dpsc');
    system('epstopdf meeting-tempThreshold.eps');
