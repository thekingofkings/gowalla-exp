
dml6 = importdata('../data_sensit_user/distance-d30-leu5000-c0.200.txt');
% dml6 = importdata('../data_sample_user/distance-d30-u5000-c0.200.txt');
    
for condition = 0;
    dml6 = dml6(dml6(:,6) > condition, :);
    [~, ind] = sort(dml6(:,8), 'descend');
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

    figure();
        hold on;
    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;

%      prec_rec( freq, friflag, 'plotROC', 0, 'holdFigure', 1, 'style', 'r');
%     prec_rec( pbg, friflag, 'plotROC', 0, 'holdFigure', 1,  'style', 'g--' );
%     prec_rec( locen, friflag, 'plotROC', 0, 'holdFigure', 1, 'style', 'b--' );
%     prec_rec( td, friflag, 'plotROC', 0, 'holdFigure', 1, 'style', 'y--');
%     prec_rec( pbg_locen, friflag, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--' );
%     prec_rec( pbg_locen_td, friflag, 'plotROC', 0, 'holdFigure', 1, 'style', 'k--' );

    % My own precision-recall plot function    


    precisionRecallPlot( freq, friflag, 'linestyle', '-', 'color', [0, 0, 0.8] );
    precisionRecallPlot( pbg, friflag, 'r--' );
    precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', [0, 0.75, 0] );
    precisionRecallPlot( td, friflag, 'linestyle', '--', 'color', [255, 215, 0] / 255 );
    precisionRecallPlot( pbg_locen, friflag, 'linestyle', '-.', 'color', [0.3, 0.6, 0.9] );
    precisionRecallPlot( pbg_locen_td, friflag, 'linestyle', '-', 'color', [0.5, 0.4, 0.9] );


%     title(num2str(condition));
    box on;
    grid on;
%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    axis([0,1,0,1]);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'Frequency', 'Personal', 'Global', 'Temp Depen', 'Per+Glo', 'Per+Glo+Tem'}, 'location', 'southwest');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
%     print(['pr-', num2str(condition), 'c5000u.eps'], '-dpsc');
%     system(['epstopdf pr-', num2str(condition), 'c5000u.eps']);

%     print(['pr-',num2str(condition),'.jpg'], '-djpeg', '-r40');
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
end