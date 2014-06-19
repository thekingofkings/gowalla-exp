
dml6 = importdata('../data/distance-d30-u5000-c0.200.txt');
    
for condition = 0:5;
    dml6 = dml6(dml6(:,6) > condition, :);
    [~, ind] = sort(dml6(:,6));
    dml6 = dml6(ind, :);  

%     sum(dml5(:,7)==1)
%     sum(dml5(:,7)==0)
    size(dml6)

    pbg_locen = dml6(:,3);
    locen = dml6(:,4);
    pbg = dml6(:,5);
    freq = dml6(:,6);
    pbg_locen_td = dml6(:,7);
    td = dml6(:,8);
    friflag = dml6(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    
    figure();
    hold on;
    precisionRecallPlot( freq, friflag, 'linestyle', '-', 'color', [0, 0, 0.8] );
    precisionRecallPlot( pbg, friflag, 'r--' );
    precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', [0, 0.75, 0] );
    precisionRecallPlot( td, friflag, 'linestyle', '--', 'color', [255, 215, 0] / 255 );
    precisionRecallPlot( pbg_locen, friflag, 'linestyle', '-', 'color', [0.3, 0.6, 0.9] );
    precisionRecallPlot( pbg_locen_td, friflag, 'linestyle', '-.', 'color', [0.5, 0.4, 0.9] );


%     title(num2str(condition));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'Frequency', 'Personal', 'Global', 'Temp Depen', 'Per+Glo', 'Per+Glo+Tem'}, 'location', 'southwest');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print(['pr-', num2str(condition), 'c5000u.eps'], '-dpsc');
    system(['epstopdf pr-', num2str(condition), 'c5000u.eps']);
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
end