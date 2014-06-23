color = char('r--', 'g--', 'b--', 'k--', 'y-', 'g-', 'c-', 'm-', 'r-', 'g:', 'b-', ...
    'k-', 'y--', 'g--', 'c--', 'm--', 'r-.', 'g-.', 'b-.', 'k-.', ...
    'y-.', 'g-.', 'c-.', 'm-.');

flist = ls('../data_tunningDC/tuneDC-u5000-t1.000-c*.txt');
fn = size(flist,1);
figure();
hold on;

para_c = zeros(fn, 1);
precisions = zeros(fn, 3);

for c = 1:fn
    para_c(c) = sscanf(flist(c,:), 'tuneDC-u5000-t1.000-c%f.txt');
    dml5 = importdata(['../data_tunningDC/', flist(c,:)]);
    dml5 = dml5(dml5(:,6) > 0, :);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);

    locm5 = dml5(:,5);
    locf5 = dml5(:,6);
    dl5 = dml5(:,9);

    precisions(c,:) =  precisionRecallPlot( locm5, dl5 );
end

    [~, ind] = sort(para_c);
    para_c = para_c(ind);
    precisions = precisions(ind, :);
    
    plot(para_c, precisions(:,1), 'd--', 'color', [0.7, 0.3, 0.3]);
    plot(para_c, precisions(:,2), 'o--', 'color', [0.3, 0.3, 0.7]);
    plot(para_c, precisions(:,3), 'v--', 'color', [0.3, 0.7, 0.3]);
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3, 'markersize', 14);
    xlabel('Distance Parameter $C_d$', 'interpreter', 'latex', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18, 'xtick', [0.01, 0.1, 1, 10, 100], ...
        'linewidth', 2, 'xscale', 'log');
    legend({'Recall 0.3', 'Recall 0.5', 'Recall 0.7'}, 'location', 'northwest');
    set(gcf, 'PaperUnits', 'inches');
%     print('turnDistC.eps', '-dpsc');
%     system('epstopdf turnDistC.eps');
    saveas(gcf, 'tuneDistC.jpg');
