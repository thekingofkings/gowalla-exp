color = char('r--', 'g--', 'b--', 'k--', 'y-', 'g-', 'c-', 'm-', 'r-', 'g:', 'b-', ...
    'k-', 'y--', 'g--', 'c--', 'm--', 'r-.', 'g-.', 'b-.', 'k-.', ...
    'y-.', 'g-.', 'c-.', 'm-.');

flist = ls('../distanceMeasure_label-u1000c*.txt');

for c = 1:size(flist,1)

    dml5 = importdata(['../', flist(c,:)]);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);

    locm5 = dml5(:,5);
    locf5 = dml5(:,6);
    dl5 = dml5(:,7);

    prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', color(c,:) );     
end

    title(num2str(c));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
%     legend({'weighted frequency', 'colocation entropy', '1 - exp measure', 'freq'}, 'location', 'best');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    % print(['prl-50m', num2str(c), 'c1000u.eps'], '-dpsc');
    % system(['epstopdf prl-50m', num2str(c), 'c1000u.eps']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
